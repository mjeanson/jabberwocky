/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.efficios.jabberwocky.ctf.trace.event;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.ClassRule;
import org.junit.Test;

import com.efficios.jabberwocky.ctf.trace.ExtractedCtfTestTrace;
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTraceIterator;
import com.efficios.jabberwocky.trace.event.field.ArrayValue;
import com.efficios.jabberwocky.trace.event.field.IntegerValue;

/**
 * Tests making sure sign extension sign extension of field values works
 * correctly.
 *
 * See: https://bugs.eclipse.org/bugs/show_bug.cgi?id=491382
 *
 * @author Alexandre Montplaisir
 */
public class CtfTmfEventFieldSignExtensionTest {

    @ClassRule
    public static final ExtractedCtfTestTrace ETT = new ExtractedCtfTestTrace(CtfTestTrace.DEBUG_INFO3);

    /**
     * Test that signed 8-byte integers are printed correctly.
     */
    @Test
    public void testUnsignedByte() {
        /*
         * Third event of the trace is printed like this by Babeltrace:
         *
         * [16:25:03.003427176] (+0.000001578) sonoshee lttng_ust_statedump:build_id:
         *      { cpu_id = 0 }, { ip = 0x7F3BBEDDDE1E, vpid = 3520 },
         *      { baddr = 0x400000, _build_id_length = 20, build_id = [ [0] = 0x1, [1] = 0xC6, [2] = 0x5, [3] = 0xBC, [4] = 0xF3, [5] = 0x8D, [6] = 0x6, [7] = 0x8D, [8] = 0x77, [9] = 0xA6, [10] = 0xE0, [11] = 0xA0, [12] = 0x2C, [13] = 0xED, [14] = 0xE6, [15] = 0xA5, [16] = 0xC, [17] = 0x57, [18] = 0x50, [19] = 0xB5 ] }
         */
        long[] expectedValues = new long[] {
                0x1,
                0xC6,
                0x5,
                0xBC,
                0xF3,
                0x8D,
                0x6,
                0x8D,
                0x77,
                0xA6,
                0xE0,
                0xA0,
                0x2C,
                0xED,
                0xE6,
                0xA5,
                0xC,
                0x57,
                0x50,
                0xB5
        };

        String expectedToString = LongStream.of(expectedValues)
                .mapToObj(i -> "0x" + Long.toHexString(i))
                .collect(Collectors.joining(", ", "[", "]"));

        try (GenericCtfTraceIterator iter = ETT.getTrace().getIterator();) {
            /* Go to third event */
            iter.next();
            iter.next();

            /* Retrieve the event's field called "build_id" */
            CtfTraceEvent event = iter.next();
            ArrayValue<IntegerValue> arrayValue = event.getField("build_id", ArrayValue.class);
            long[] values = IntStream.range(0, arrayValue.size())
                    .mapToObj(arrayValue::getElement)
                    .mapToLong(IntegerValue::getValue)
                    .toArray();

            assertArrayEquals(expectedValues, values);
            assertEquals(expectedToString, arrayValue.toString());
        }
    }
}
