/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.views.threads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.stream.Collectors;

import javax.management.AttributeNotFoundException;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.efficios.jabberwocky.trace.ITrace;
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateInterval;
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateRender;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.StateSystemUtils;
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException;
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval;

/**
 * Tests for {@link ControlFlowRenderProvider}.
 *
 * @author Alexandre Montplaisir
 */
public class ControlFlowRenderProviderTest {

//    /** Timeout the tests after 2 minutes */
//    @Rule
//    public TestRule timeoutRule = new Timeout(2, TimeUnit.MINUTES);

    private static final long NANOS_PER_SECOND = 1000000000L;

    private static final @NonNull CtfTestTrace TEST_TRACE = CtfTestTrace.KERNEL;

    private static ITrace sfTrace;
    private static ITmfStateSystem sfSS;

    private ControlFlowRenderProvider provider = new ControlFlowRenderProvider();
    {
        provider.disableFilterMode(0);
    }

    /**
     * Test class setup
     */
    @Before
    public void setupClass() {
        ITrace trace = KernelCtfTraceStub.getTrace(TEST_TRACE);
        trace.traceOpened(new TmfTraceOpenedSignal(ControlFlowRenderProviderTest.class, trace, null));

        IAnalysisModule analysis = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelAnalysisModule.class, KernelAnalysisModule.ID);
        assertNotNull(analysis);
        analysis.schedule(); // Should have run, just in case
        analysis.waitForCompletion();

        ITmfStateSystem ss = TmfStateSystemAnalysisModule.getStateSystem(trace, KernelAnalysisModule.ID);
        assertNotNull(ss);

        sfTrace = trace;
        sfSS = ss;

        provider.setTrace(trace);
    }

    /**
     * Test class teardown
     */
    @After
    public void teardownClass() {
        if (sfTrace != null) {
            /* Trace's dispose will dispose its state systems */
            sfTrace.dispose();
        }

        provider.setTrace(null);
    }

    /**
     * Check that the info in a render for the first second of the trace matches
     * the corresponding info found in the state system.
     */
    @Test
    public void test1s() {
        try {
            final ITrace trace = sfTrace;
            final ITmfStateSystem ss = sfSS;
            assertNotNull(trace);
            assertNotNull(ss);

            final long start = trace.getStartTime().toNanos();
            final long end = start + 1 * NANOS_PER_SECOND;


            /* Check that the list of attributes (tree render) are the same */
            TimeGraphTreeRender treeRender = provider.getTreeRender(start, end);
            List<TimeGraphTreeElement> treeElems = treeRender.getAllTreeElements();

            List<String> tidsFromRender = treeElems.stream()
                    .map(e -> (ControlFlowTreeElement) e)
                    .mapToInt(ControlFlowTreeElement::getTid)
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
            List<String> tidsInSS = ss.getQuarks(threadsQuark, "*").stream()
                    .map(ss::getAttributeName)
                    .sorted()
                    .collect(Collectors.toList());

            for (String tid : tidsInSS) {
                int threadQuark = ss.getQuarkRelative(threadsQuark, tid);
                List<ITmfStateInterval> intervalsFromSS =
                        StateSystemUtils.queryHistoryRange(ss, threadQuark, start, end);

                TimeGraphTreeElement elem = treeElems.stream()
                        .map(e -> (ControlFlowTreeElement) e)
                        .filter(e -> e.getSourceQuark() == threadQuark)
                        .findFirst()
                        .get();

                TimeGraphStateRender stateRender = provider.getStateRender(elem, start, end, 1);
                List<TimeGraphStateInterval> intervalsFromRender = stateRender.getStateIntervals();

                verifySameIntervals(intervalsFromSS, intervalsFromRender);
                // TODO Also verify against known hard-coded list
            }

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    private static void verifySameIntervals(List<ITmfStateInterval> ssIntervals,
            List<TimeGraphStateInterval> renderIntervals) {
        assertEquals(ssIntervals.size(), renderIntervals.size());

        for (int i = 0; i < ssIntervals.size(); i++) {
            ITmfStateInterval ssInterval = ssIntervals.get(i);
            TimeGraphStateInterval renderInterval = renderIntervals.get(i);

            assertEquals(ssInterval.getStartTime(), renderInterval.getStartEvent().getTimestamp());
            assertEquals(ssInterval.getEndTime(), renderInterval.getEndEvent().getTimestamp());

            int stateValue = ssInterval.getStateValue().unboxInt();
            String stateName = ControlFlowRenderProvider.mapStateValueToStateName(stateValue);
            assertEquals(stateName, renderInterval.getStateName());
        }
    }
}
