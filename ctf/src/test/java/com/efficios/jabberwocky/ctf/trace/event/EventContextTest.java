/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package com.efficios.jabberwocky.ctf.trace.event;

import static org.junit.Assert.assertEquals;

import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.efficios.jabberwocky.ctf.trace.ExtractedCtfTestTrace;
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTraceIterator;
import com.efficios.jabberwocky.trace.event.field.IntegerValue;
import com.efficios.jabberwocky.trace.event.field.StringValue;

/**
 * Tests for reading event contexts from a CtfTmfTrace.
 *
 * @author Alexandre Montplaisir
 */
public class EventContextTest {

    /* We use test trace #2, kernel_vm, which has event contexts */
    @ClassRule
    public static final ExtractedCtfTestTrace ETT = new ExtractedCtfTestTrace(CtfTestTrace.KERNEL_VM);

    private long startTime;
    private long endTime;

    @Before
    public void setup() {
        startTime = ETT.getTrace().getStartTime();
        // TODO Query this timestamp from the trace itself
        endTime = 1363700770550261288L;
    }

    /**
     * Make sure the trace is the correct one, and its timestamps are read
     * correctly.
     */
    @Test
    public void testTrace() {
        assertEquals(1363700740555978750L, startTime);
        assertEquals(1363700770550261288L, endTime);
    }

    /**
     * Test the context of the very first event of the trace.
     */
    @Test
    public void testContextStart() {
        CtfTraceEvent firstEvent = getEventAt(startTime);
        long perfPageFault = firstEvent.getField("context.perf_page_fault", IntegerValue.class).getValue();
        String procname = firstEvent.getField("context.procname", StringValue.class).getValue();
        long tid = firstEvent.getField("context.tid", IntegerValue.class).getValue();

        assertEquals(613, perfPageFault);
        assertEquals("lttng-sessiond", procname);
        assertEquals(1230, tid);
    }

    /**
     * Test the context of the event at 1363700745.559739078.
     */
    @Test
    public void testContext1() {
        long time = startTime + 5000000000L; // 1363700745.559739078
        CtfTraceEvent event = getEventAt(time);
        long perfPageFault = event.getField("context.perf_page_fault", IntegerValue.class).getValue();
        String procname = event.getField("context.procname", StringValue.class).getValue();
        long tid = event.getField("context.tid", IntegerValue.class).getValue();

        assertEquals(6048, perfPageFault);
        assertEquals("swapper/0", procname);
        assertEquals(0, tid);
    }

    /**
     * Test the context of the event at 1363700750.559707062.
     */
    @Test
    public void testContext2() {
        long time = startTime + 2 * 5000000000L; // 1363700750.559707062
        CtfTraceEvent event = getEventAt(time);
        long perfPageFault = event.getField("context.perf_page_fault", IntegerValue.class).getValue();
        String procname = event.getField("context.procname", StringValue.class).getValue();
        long tid = event.getField("context.tid", IntegerValue.class).getValue();

        assertEquals(13258, perfPageFault);
        assertEquals("swapper/0", procname);
        assertEquals(0, tid);
    }

    /**
     * Test the context of the event at 1363700755.555723128, which is roughly
     * mid-way through the trace.
     */
    @Test
    public void testContextMiddle() {
        long midTime = startTime + (endTime - startTime) / 2L; // 1363700755.555723128
        CtfTraceEvent midEvent = getEventAt(midTime);
        long perfPageFault = midEvent.getField("context.perf_page_fault", IntegerValue.class).getValue();
        String procname = midEvent.getField("context.procname", StringValue.class).getValue();
        long tid = midEvent.getField("context.tid", IntegerValue.class).getValue();

        assertEquals(19438, perfPageFault);
        assertEquals("swapper/0", procname);
        assertEquals(0, tid);
    }

    /**
     * Test the context of the event at 1363700760.559719724.
     */
    @Test
    public void testContext3() {
        long time = startTime + 4 * 5000000000L; // 1363700760.559719724
        CtfTraceEvent event = getEventAt(time);
        long perfPageFault = event.getField("context.perf_page_fault", IntegerValue.class).getValue();
        String procname = event.getField("context.procname", StringValue.class).getValue();
        long tid = event.getField("context.tid", IntegerValue.class).getValue();

        assertEquals(21507, perfPageFault);
        assertEquals("swapper/0", procname);
        assertEquals(0, tid);
    }

    /**
     * Test the context of the event at 1363700765.559714634.
     */
    @Test
    public void testContext4() {
        long time = startTime + 5 * 5000000000L; // 1363700765.559714634
        CtfTraceEvent event = getEventAt(time);
        long perfPageFault = event.getField("context.perf_page_fault", IntegerValue.class).getValue();
        String procname = event.getField("context.procname", StringValue.class).getValue();
        long tid = event.getField("context.tid", IntegerValue.class).getValue();

        assertEquals(21507, perfPageFault);
        assertEquals("swapper/0", procname);
        assertEquals(0, tid);
    }

    /**
     * Test the context of the last event of the trace.
     */
    @Test
    public void testContextEnd() {
        CtfTraceEvent lastEvent = getEventAt(endTime);
        long perfPageFault = lastEvent.getField("context.perf_page_fault", IntegerValue.class).getValue();
        String procname = lastEvent.getField("context.procname", StringValue.class).getValue();
        long tid = lastEvent.getField("context.tid", IntegerValue.class).getValue();

        assertEquals(22117, perfPageFault);
        assertEquals("lttng-sessiond", procname);
        assertEquals(1230, tid);
    }

    // ------------------------------------------------------------------------
    // Private stuff
    // ------------------------------------------------------------------------

    private synchronized CtfTraceEvent getEventAt(long timestamp) {
        try (GenericCtfTraceIterator iter = ETT.getTrace().getIterator();) {
            while (iter.hasNext()) {
                CtfTraceEvent event = iter.next();
                if (event.getTimestamp() >= timestamp) {
                    return event;
                }
            }
        }
        throw new IllegalArgumentException("No event with timestamp " + timestamp + " found.");
    }

}
