/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson

 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

 * Contributors:
 * Alexandre Montplaisir - Initial API and implementation
 */

package com.efficios.jabberwocky.ctf.trace

import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.trace.event.FieldValue.*
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit

/**
 * More advanced CTF tests using "funky_trace", a trace generated with the
 * Babeltrace CTF writer API, which has lots of fun things like different
 * integer/float sizes and non-standard struct alignments.

 * @author Alexandre Montplaisir
 */
class FunkyTraceTest {

    companion object {

        @JvmField @ClassRule
        val ETT = ExtractedCtfTestTrace(CtfTestTrace.FUNKY_TRACE)

        const val DELTA = 0.0000001
    }

    /** Time-out tests after 1 minute.  */
    @JvmField @Rule
    val globalTimeout: TestRule = Timeout(1, TimeUnit.MINUTES)

    /**
     * Verify the contents of the first event
     */
    @Test
    fun testFirstEvent() {
        val event = getEvent(0)
        assertEquals("Simple Event", event.eventName)
        assertEquals(1234567L, event.timestamp)
        assertEquals(42L, event.getField("integer_field", IntegerValue::class.java)!!.value)
        assertEquals(3.1415, event.getField("float_field", FloatValue::class.java)!!.value, DELTA)
    }

    /**
     * Verify the contents of the second event (the first "spammy event")
     */
    @Test
    fun testSecondEvent() {
        val event = getEvent(1)
        assertEquals("Spammy_Event", event.eventName)
        assertEquals(1234568L, event.timestamp)
        assertEquals(0L, event.getField("field_1", IntegerValue::class.java)!!.value)
        assertEquals("This is a test", event.getField("a_string", StringValue::class.java)!!.value)
    }

    /**
     * Verify the contents of the last "spammy event"
     */
    @Test
    fun testSecondToLastEvent() {
        val event = getEvent(100000)
        assertEquals("Spammy_Event", event.eventName)
        assertEquals(1334567L, event.timestamp)
        assertEquals(99999L, event.getField("field_1", IntegerValue::class.java)!!.value)
        assertEquals("This is a test", event.getField("a_string", StringValue::class.java)!!.value)
    }

    /**
     * Verify the contents of the last, complex event
     */
    @Test
    fun testLastEvent() {
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

        val event = getEvent(100001)
        assertEquals("Complex Test Event", event.eventName)
        assertEquals(1334568L, event.timestamp)
        assertEquals(0xddf00dL, event.getField("uint_35", IntegerValue::class.java)!!.value)
        assertEquals(-12345L, event.getField("int_16", IntegerValue::class.java)!!.value)

        val complexStruct = event.getField("complex_structure", StructValue::class.java)!!
        val fieldNames = complexStruct.fieldNames
        assertTrue(fieldNames.contains("variant_selector"))
        assertTrue(fieldNames.contains("a_string"))
        assertTrue(fieldNames.contains("variant_value"))

        val enumVal = complexStruct.getField("variant_selector", EnumValue::class.java)
        assertEquals("INT16_TYPE", enumVal?.stringValue)
        assertEquals(1L, enumVal?.longValue)

        assertEquals("Test string", complexStruct.getField("a_string", StringValue::class.java)!!.value)

        val variantField = complexStruct.getField("variant_value", IntegerValue::class.java)!!
        assertEquals(-200L, variantField.value)

        val innerStruct = complexStruct.getField("inner_structure", StructValue::class.java)!!
        assertEquals(10L, innerStruct.getField("seq_len", IntegerValue::class.java)?.value)

        // FIXME Replace Class<?> parameters with something better
        val arrayVal = innerStruct.getField("a_sequence", ArrayValue::class.java)!!
        val expectedValues = longArrayOf(4, 3, 2, 1, 0, -1, -2, -3, -4, -5)
        assertEquals(expectedValues.size, arrayVal.size)
        for (i in expectedValues.indices) {
            // Maybe we can do something with reified parameters dark magic
            val elem = arrayVal.getElement(i) as IntegerValue
            assertEquals(expectedValues[i], elem.value)
        }
    }

    // ------------------------------------------------------------------------
    // Private stuff
    // ------------------------------------------------------------------------

    private fun getEvent(index: Long): CtfTraceEvent {
        ETT.trace.iterator().use {
            for (remaining in index downTo 1) {
                it.next()
            }
            return it.next()
        }
    }

}
