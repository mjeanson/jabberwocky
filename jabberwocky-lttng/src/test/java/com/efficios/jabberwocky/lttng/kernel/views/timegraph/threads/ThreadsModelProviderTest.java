/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.efficios.jabberwocky.lttng.kernel.views.timegraph.threads;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.StateSystemUtils;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue;
import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.Attributes;
import com.efficios.jabberwocky.lttng.testutils.ExtractedLttngKernelTestTrace;
import com.efficios.jabberwocky.project.ITraceProject;
import com.efficios.jabberwocky.project.TraceProject;
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateInterval;
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateRender;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import com.google.common.collect.Iterables;
import com.google.common.io.MoreFiles;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.*;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Tests for {@link ThreadsModelProvider}.
 *
 * @author Alexandre Montplaisir
 */
public class ThreadsModelProviderTest {

    @ClassRule
    public static final ExtractedLttngKernelTestTrace TEST_TRACE = new ExtractedLttngKernelTestTrace(CtfTestTrace.KERNEL);

    /** Timeout the tests after 2 minutes */
    @Rule
    public TestRule timeoutRule = new Timeout(2, TimeUnit.MINUTES);

    private static final String PROJECT_NAME = "test-proj";
    private static final long NANOS_PER_SECOND = 1000000000L;

    private ThreadsModelProvider provider = new ThreadsModelProvider();
    {
        provider.disableFilterMode(0);
    }

    private Path projectPath;

    /**
     * Test class setup
     */
    @Before
    public void setupClass() {
        try {
            projectPath = Files.createTempDirectory(PROJECT_NAME);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        ITraceProject project = TraceProject.ofSingleTrace(PROJECT_NAME, projectPath, TEST_TRACE.getTrace());
        provider.setTraceProject(project);
    }

    /**
     * Test class teardown
     */
    @After
    public void teardownClass() {
        provider.setTraceProject(null);

        if (projectPath != null) {
            try {
                MoreFiles.deleteRecursively(projectPath);
            } catch (IOException e) {
                /* Ignore */
            }
        }
    }

    /**
     * Check that the info in a render for the first second of the trace matches
     * the corresponding info found in the state system.
     */
    @Test
    public void test1s() {
        try {

            final ITmfStateSystem ss = provider.getStateSystem();
            assertNotNull(ss);

            final long start = provider.getTraceProject().getStartTime();
            final long end = start + 1 * NANOS_PER_SECOND;
            final TimeRange range = TimeRange.of(start, end);


            /* Check that the list of attributes (tree render) are the same */
            TimeGraphTreeRender treeRender = provider.getTreeRender();
            List<TimeGraphTreeElement> treeElems = treeRender.getAllTreeElements();

            List<String> tidsFromRender = treeElems.stream()
                    .filter(e -> e instanceof ThreadsTreeElement).map(e -> (ThreadsTreeElement) e)
                    .mapToInt(ThreadsTreeElement::getTid)
                    .mapToObj(tid -> String.valueOf(tid))
                    .sorted()
                    .collect(Collectors.toList());

            int threadsQuark = ss.getQuarkAbsolute(Attributes.THREADS);
            List<String> tidsFromSS = ss.getSubAttributes(threadsQuark, false).stream()
                    .map(quark -> ss.getAttributeName(quark))
                    .map(name -> {
                        if (name.startsWith(Attributes.THREAD_0_PREFIX)) {
                            return "0";
                        }
                        return name;
                    })
                    .sorted()
                    .collect(Collectors.toList());

            assertEquals(tidsFromSS, tidsFromRender);
            // TODO Also verify against known hard-coded list


            /* Check that the state intervals are the same */
            List<String> tidsInSS = ss.getSubAttributes(threadsQuark, false).stream()
                    .map(ss::getAttributeName)
                    .sorted()
                    .collect(Collectors.toList());

            for (String tid : tidsInSS) {
                int threadQuark = ss.getQuarkRelative(threadsQuark, tid);
                List<ITmfStateInterval> intervalsFromSS =
                        StateSystemUtils.queryHistoryRange(ss, threadQuark, start, end);

                TimeGraphTreeElement elem = treeElems.stream()
                        .filter(e -> e instanceof ThreadsTreeElement).map(e -> (ThreadsTreeElement) e)
                        .filter(e -> e.getSourceQuark() == threadQuark)
                        .findFirst()
                        .get();

                TimeGraphStateRender stateRender = provider.getStateProvider().getStateRender(elem, range, 1, null);
                List<TimeGraphStateInterval> intervalsFromRender = stateRender.getStateIntervals();

                verifySameIntervals(intervalsFromSS, intervalsFromRender);
                // TODO Also verify against known hard-coded list
            }

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Verify that for a known time range, all generated intervals are
     * contiguous but of a different states (multi-states are included in
     * there).
     */
    @Test
    public void testMultiStates() {
        TimeRange range = TimeRange.of(1332170683505733202L, 1332170683603572392L);
        String treeElemName = "0/0 - swapper";
        long viewWidth = 1000;

        long resolution = range.getDuration() / viewWidth;

        TimeGraphTreeElement treeElem = provider.getTreeRender().getAllTreeElements().stream()
                .filter(elem -> elem.getName().equals(treeElemName))
                .findFirst().get();
        TimeGraphStateRender stateRender = provider.getStateProvider().getStateRender(treeElem, range, resolution, null);
        List<TimeGraphStateInterval> intervals = stateRender.getStateIntervals();

        assertTrue(intervals.size() > 2);

        for (int i = 1; i < intervals.size(); i++) {
            TimeGraphStateInterval interval1 = intervals.get(i - 1);
            TimeGraphStateInterval interval2 = intervals.get(i);

            assertEquals(interval1.getEndTime() + 1, interval2.getStartTime());
            assertNotEquals(interval1.getStateName(), interval2.getStateName());
        }
    }

    /**
     * Make sure that if multi-states are present at the beginning or end of a
     * time graph render, they actually start/end at the same timestamps as the
     * full state model.
     */
    @Test
    public void testBounds() {
        final ITraceProject project = provider.getTraceProject();
        final ITmfStateSystem ss = provider.getStateSystem();
        assertNotNull(project);
        assertNotNull(ss);

        /*
         * Note that here, the range of the query is the full range of the
         * trace, so the start/end times of the full state system should match
         * the ones in the model. This might not always be the case with
         * multi-states at the beginning/end, since those may have synthetic
         * start/end times.
         */
        TimeRange range = TimeRange.of(project.getStartTime(), project.getEndTime());
        String treeElemName = "0/0 - swapper";
        long viewWidth = 1000;

        long resolution = range.getDuration() / viewWidth;

        /* Get the intervals from the model */
        TimeGraphTreeElement treeElem = provider.getTreeRender().getAllTreeElements().stream()
                .filter(elem -> elem.getName().equals(treeElemName))
                .findFirst().get();
        TimeGraphStateRender stateRender = provider.getStateProvider().getStateRender(treeElem, range, resolution, null);
        List<TimeGraphStateInterval> intervalsFromRender = stateRender.getStateIntervals();

        /* Get the intervals from the state system */
        int threadsQuark = ss.getQuarkAbsolute(Attributes.THREADS);
        int threadQuark = ss.getQuarkRelative(threadsQuark, "0_0");
        List<ITmfStateInterval> intervalsFromSS;
        try {
            intervalsFromSS = StateSystemUtils.queryHistoryRange(ss, threadQuark, range.getStartTime(), range.getEndTime());
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
            return;
        }

        /* Check that the first intervals start at the same timestamp. */
        long modelStart = intervalsFromRender.get(0).getStartTime();
        long ssStart = intervalsFromSS.get(0).getStartTime();
        assertEquals(ssStart, modelStart);

        /* Check that the last intervals end at the same timestamp too. */
        long modelEnd = Iterables.getLast(intervalsFromRender).getEndTime();
        long ssEnd = Iterables.getLast(intervalsFromSS).getEndTime();
        assertEquals(ssEnd, modelEnd);
    }

    private static void verifySameIntervals(List<ITmfStateInterval> ssIntervals,
            List<TimeGraphStateInterval> renderIntervals) {
        assertEquals(ssIntervals.size(), renderIntervals.size());

        for (int i = 0; i < ssIntervals.size(); i++) {
            ITmfStateInterval ssInterval = ssIntervals.get(i);
            TimeGraphStateInterval renderInterval = renderIntervals.get(i);

            assertEquals(ssInterval.getStartTime(), renderInterval.getStartEvent().getTimestamp());
            assertEquals(ssInterval.getEndTime(), renderInterval.getEndEvent().getTimestamp());

            ITmfStateValue stateValue = ssInterval.getStateValue();
            String stateName = ThreadsModelStateProvider.stateValueToStateDef(stateValue).getName();
            assertEquals(stateName, renderInterval.getStateName());
        }
    }
}
