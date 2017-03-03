/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package com.efficios.jabberwocky.ctf.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent;
import com.efficios.jabberwocky.trace.event.field.ArrayValue;
import com.efficios.jabberwocky.trace.event.field.EnumValue;
import com.efficios.jabberwocky.trace.event.field.FloatValue;
import com.efficios.jabberwocky.trace.event.field.IntegerValue;
import com.efficios.jabberwocky.trace.event.field.StringValue;
import com.efficios.jabberwocky.trace.event.field.StructValue;

/**
 * More advanced CTF tests using "funky_trace", a trace generated with the
 * Babeltrace CTF writer API, which has lots of fun things like different
 * integer/float sizes and non-standard struct alignments.
 *
 * @author Alexandre Montplaisir
 */
public class FunkyTraceTest {

    @ClassRule
    public static final ExtractedCtfTestTrace ETT = new ExtractedCtfTestTrace(CtfTestTrace.FUNKY_TRACE);

    /** Time-out tests after 1 minute. */
    @Rule
    public TestRule globalTimeout = new Timeout(1, TimeUnit.MINUTES);

    private static final double DELTA = 0.0000001;

    /**
     * Verify the contents of the first event
     */
    @Test
    public void testFirstEvent() {
        CtfTraceEvent event = getEvent(0);
        assertEquals("Simple Event", event.getEventName());
        assertEquals(1234567L, event.getTimestamp());
        assertEquals(42L, event.getField("integer_field", IntegerValue.class).getValue());
        assertEquals(3.1415, event.getField("float_field", FloatValue.class).getValue(), DELTA);
    }

    /**
     * Verify the contents of the second event (the first "spammy event")
     */
    @Test
    public void testSecondEvent() {
        CtfTraceEvent event = getEvent(1);
        assertEquals("Spammy_Event", event.getEventName());
        assertEquals(1234568L, event.getTimestamp());
        assertEquals(0L, event.getField("field_1", IntegerValue.class).getValue());
        assertEquals("This is a test", event.getField("a_string", StringValue.class).getValue());
    }

    /**
     * Verify the contents of the last "spammy event"
     */
    @Test
    public void testSecondToLastEvent() {
        CtfTraceEvent event = getEvent(100000);
        assertEquals("Spammy_Event", event.getEventName());
        assertEquals(1334567L, event.getTimestamp());
        assertEquals(99999L, event.getField("field_1", IntegerValue.class).getValue());
        assertEquals("This is a test", event.getField("a_string", StringValue.class).getValue());
    }

    /**
     * Verify the contents of the last, complex event
     */
    @Test
    public void testLastEvent() {
        /*
         * Last event as seen in Babeltrace:
         * [19:00:00.001334568] (+0.000000001) Complex Test Event: { }, {
         *     uint_35 = 0xDDF00D,
         *     int_16 = -12345,
         *     complex_structure = {
         *         variant_selector = ( INT16_TYPE : container = 1 ),
         *         a_string = "Test string",
         *         variant_value = { INT16_TYPE = -200 },
         *         inner_structure = {
         *             seq_len = 0xA,
         *             a_sequence = [ [0] = 4, [1] = 3, [2] = 2, [3] = 1, [4] = 0, [5] = -1, [6] = -2, [7] = -3, [8] = -4, [9] = -5 ]
         *         }
         *     }
         * }
         */

        CtfTraceEvent event = getEvent(100001);
        assertEquals("Complex Test Event", event.getEventName());
        assertEquals(1334568L, event.getTimestamp());
        assertEquals(0xddf00d, event.getField("uint_35", IntegerValue.class).getValue());
        assertEquals(-12345, event.getField("int_16", IntegerValue.class).getValue());

        StructValue complexStruct = event.getField("complex_structure", StructValue.class);
        Set<String> fieldNames = complexStruct.getFieldNames();
        assertTrue(fieldNames.contains("variant_selector"));
        assertTrue(fieldNames.contains("a_string"));
        assertTrue(fieldNames.contains("variant_value"));

        EnumValue enumVal = complexStruct.getField("variant_selector", EnumValue.class);
        assertEquals("INT16_TYPE", enumVal.getStringValue());
        assertEquals(1L, enumVal.getLongValue());

        assertEquals("Test string", complexStruct.getField("a_string", StringValue.class).getValue());

        IntegerValue variantField = complexStruct.getField("variant_value", IntegerValue.class);
        assertEquals(-200L, variantField.getValue());

        StructValue innerStruct = complexStruct.getField("inner_structure", StructValue.class);
        assertEquals(10L, innerStruct.getField("seq_len", IntegerValue.class).getValue());

        ArrayValue<IntegerValue> arrayVal = innerStruct.getField("a_sequence", ArrayValue.class);
        long[] expectedValues = { 4, 3, 2, 1, 0, -1, -2, -3, -4, -5 };
        assertEquals(expectedValues.length, arrayVal.size());
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], arrayVal.getElement(i).getValue());
        }
    }

    // ------------------------------------------------------------------------
    // Private stuff
    // ------------------------------------------------------------------------

    private synchronized CtfTraceEvent getEvent(long index) {
        CtfTraceIterator<CtfTraceEvent> iter = ETT.getTrace().getIterator();
        for (long remaining = index; remaining > 0; remaining--) {
            iter.next();
        }
        return iter.next();
    }

}
