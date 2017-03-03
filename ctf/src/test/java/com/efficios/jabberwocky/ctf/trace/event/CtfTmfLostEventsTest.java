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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.ClassRule;
import org.junit.Test;

import com.efficios.jabberwocky.ctf.trace.ExtractedCtfTestTrace;
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTrace;
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTraceIterator;
import com.efficios.jabberwocky.trace.event.ITraceLostEvent;

/**
 * Tests to verify that lost events are handled correctly.
 *
 * Be wary if you are using Babeltrace to cross-check those values. There could
 * be a bug in Babeltrace with regards to lost events. See
 * http://bugs.lttng.org/issues/589
 *
 * It's not 100% sure at this point which implementation is correct, so for now
 * these tests assume the Java implementation is the right one.
 *
 * @author Alexandre Montplaisir
 */
public class CtfTmfLostEventsTest {

    @ClassRule
    public static final ExtractedCtfTestTrace HELLO_LOST_TT = new ExtractedCtfTestTrace(CtfTestTrace.HELLO_LOST);

    @ClassRule
    public static final ExtractedCtfTestTrace DYNSCOPE_TT = new ExtractedCtfTestTrace(CtfTestTrace.DYNSCOPE);

    /**
     * Test that the number of events is reported correctly (a range of lost
     * events is counted as one event).
     */
    @Test
    public void testNbEvents() {
        final long expectedReal = 32300;
        final long expectedLost = 562;

        EventCountRequest req = new EventCountRequest(HELLO_LOST_TT.getTrace());

        assertEquals(expectedReal, req.getReal());
        assertEquals(expectedLost, req.getLost());
    }

    /**
     * Test that the number of events is reported correctly (a range of lost
     * events is counted as one event). Events could be wrongly counted as lost
     * events in certain situations.
     */
    @Test
    public void testNbEventsBug475007() {
        final long expectedReal = 100003;
        final long expectedLost = 1;

        EventCountRequest req = new EventCountRequest(DYNSCOPE_TT.getTrace());

        assertEquals(expectedReal, req.getReal());
        assertEquals(expectedLost, req.getLost());
    }

    /**
     * Test getting the first lost event from the trace.
     */
    @Test
    public void testFirstLostEvent() {
        GenericCtfTrace trace = HELLO_LOST_TT.getTrace();
        final long rank = 190;
        final long start = 1376592664828900165L;
        final long end = start + 502911L;
        final long nbLost = 859;

        validateLostEvent(trace, rank, start, end, nbLost);
    }

    /**
     * Test getting the second lost event from the trace.
     */
    @Test
    public void testSecondLostEvent() {
        GenericCtfTrace trace = HELLO_LOST_TT.getTrace();
        final long rank = 229;
        final long start = 1376592664829477058L;
        final long end = start + 347456L;
        final long nbLost = 488;

        validateLostEvent(trace, rank, start, end, nbLost);
    }

    /**
     * Test getting one normal event from the trace (lost events should not
     * interfere).
     */
    @Test
    public void testNormalEvent() {
        GenericCtfTrace trace = HELLO_LOST_TT.getTrace();
        final long rank = 200;
        final long ts = 1376592664829425780L;

        final CtfTraceEvent event = getEventAtTimestamp(trace, ts);
        /* Make sure seeking by rank yields the same event */
        final CtfTraceEvent event2 = getEventAtRank(trace, rank);
        assertEquals(event, event2);

        assertFalse(event instanceof ITraceLostEvent);
        assertEquals(ts, event.getTimestamp());
    }

    // ------------------------------------------------------------------------
    // Event requests
    // ------------------------------------------------------------------------

    private static void validateLostEvent(GenericCtfTrace trace, long rank, long start, long end, long nbLost) {
        final CtfTraceEvent ev = getLostEventAtTimestamp(trace, start);
        /* Make sure seeking by rank yields the same event */
        final CtfTraceEvent ev2 = getEventAtRank(trace, rank);
        assertEquals(ev, ev2);

        assertTrue(ev instanceof ITraceLostEvent);
        ITraceLostEvent event = (ITraceLostEvent) ev;

        assertEquals(start, event.getTimestamp());
        assertEquals(start, event.getTimeRange().lowerEndpoint().longValue());
        assertEquals(end, event.getTimeRange().upperEndpoint().longValue());
        assertEquals(nbLost, event.getNbLostEvents());
    }

    private static CtfTraceEvent getEventAtTimestamp(GenericCtfTrace trace, long timestamp) {
        try (GenericCtfTraceIterator iter = trace.getIterator();) {
            while (iter.hasNext()) {
                CtfTraceEvent event = iter.next();
                if (event.getTimestamp() >= timestamp) {
                    return event;
                }
            }
        }
        throw new IllegalArgumentException("No event with timestamp " + timestamp + " found.");
    }

    private static CtfTraceEvent getLostEventAtTimestamp(GenericCtfTrace trace, long timestamp) {
        try (GenericCtfTraceIterator iter = trace.getIterator();) {
            while (iter.hasNext()) {
                CtfTraceEvent event = iter.next();
                if (event.getTimestamp() >= timestamp && event instanceof ITraceLostEvent) {
                    return event;
                }
            }
        }
        throw new IllegalArgumentException("No event with timestamp " + timestamp + " found.");
    }

    private static CtfTraceEvent getEventAtRank(GenericCtfTrace trace, long rank) {
        try (GenericCtfTraceIterator iter = trace.getIterator()) {
            for (long remaining = rank; remaining > 0; remaining--) {
                iter.next();
            }
            return iter.next();
        }
    }

    private static class EventCountRequest {

        private long nbReal = 0;
        private long nbLost = 0;

        public EventCountRequest(GenericCtfTrace trace) {
            try (GenericCtfTraceIterator iter = trace.getIterator();) {
                while (iter.hasNext()) {
                    CtfTraceEvent event = iter.next();
                    if (event instanceof ITraceLostEvent) {
                        nbLost++;
                    } else {
                        nbReal++;
                    }
                }
            }
        }

        public long getReal() {
            return nbReal;
        }

        public long getLost() {
            return nbLost;
        }
    }
}
