/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson

 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

 * Contributors:
 * Alexandre Montplaisir - Initial API and implementation
 */

package com.efficios.jabberwocky.ctf.trace.event

import com.efficios.jabberwocky.ctf.trace.ExtractedCtfTestTrace
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue
import com.efficios.jabberwocky.trace.event.FieldValue.StringValue
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

/**
 * Tests for reading event contexts from a CtfTmfTrace.

 * @author Alexandre Montplaisir
 */
class EventContextTest {

    companion object {
        /* We use test trace #2, kernel_vm, which has event contexts */
        @JvmField @ClassRule
        val ETT = ExtractedCtfTestTrace(CtfTestTrace.KERNEL_VM)
    }

    private var startTime: Long = 0
    private var endTime: Long = 0

    @Before
    fun setup() {
        startTime = ETT.trace.startTime
        // TODO Query this timestamp from the trace itself
        endTime = 1363700770550261288L
    }

    /**
     * Make sure the trace is the correct one, and its timestamps are read
     * correctly.
     */
    @Test
    fun testTrace() {
        assertEquals(1363700740555978750L, startTime)
        assertEquals(1363700770550261288L, endTime)
    }

    /**
     * Test the context of the very first event of the trace.
     */
    @Test
    fun testContextStart() {
        val firstEvent = getEventAt(startTime)
        val perfPageFault = firstEvent.getField("context.perf_page_fault", IntegerValue::class.java)!!.value
        val procname = firstEvent.getField("context.procname", StringValue::class.java)!!.value
        val tid = firstEvent.getField("context.tid", IntegerValue::class.java)!!.value

        assertEquals(613, perfPageFault)
        assertEquals("lttng-sessiond", procname)
        assertEquals(1230, tid)
    }

    /**
     * Test the context of the event at 1363700745.559739078.
     */
    @Test
    fun testContext1() {
        val time = startTime + 5000000000L // 1363700745.559739078
        val event = getEventAt(time)
        val perfPageFault = event.getField("context.perf_page_fault", IntegerValue::class.java)!!.value
        val procname = event.getField("context.procname", StringValue::class.java)!!.value
        val tid = event.getField("context.tid", IntegerValue::class.java)!!.value

        assertEquals(6048, perfPageFault)
        assertEquals("swapper/0", procname)
        assertEquals(0, tid)
    }

    /**
     * Test the context of the event at 1363700750.559707062.
     */
    @Test
    fun testContext2() {
        val time = startTime + 2 * 5000000000L // 1363700750.559707062
        val event = getEventAt(time)
        val perfPageFault = event.getField("context.perf_page_fault", IntegerValue::class.java)!!.value
        val procname = event.getField("context.procname", StringValue::class.java)!!.value
        val tid = event.getField("context.tid", IntegerValue::class.java)!!.value

        assertEquals(13258, perfPageFault)
        assertEquals("swapper/0", procname)
        assertEquals(0, tid)
    }

    /**
     * Test the context of the event at 1363700755.555723128, which is roughly
     * mid-way through the trace.
     */
    @Test
    fun testContextMiddle() {
        val midTime = startTime + (endTime - startTime) / 2L // 1363700755.555723128
        val midEvent = getEventAt(midTime)
        val perfPageFault = midEvent.getField("context.perf_page_fault", IntegerValue::class.java)!!.value
        val procname = midEvent.getField("context.procname", StringValue::class.java)!!.value
        val tid = midEvent.getField("context.tid", IntegerValue::class.java)!!.value

        assertEquals(19438, perfPageFault)
        assertEquals("swapper/0", procname)
        assertEquals(0, tid)
    }

    /**
     * Test the context of the event at 1363700760.559719724.
     */
    @Test
    fun testContext3() {
        val time = startTime + 4 * 5000000000L // 1363700760.559719724
        val event = getEventAt(time)
        val perfPageFault = event.getField("context.perf_page_fault", IntegerValue::class.java)!!.value
        val procname = event.getField("context.procname", StringValue::class.java)!!.value
        val tid = event.getField("context.tid", IntegerValue::class.java)!!.value

        assertEquals(21507, perfPageFault)
        assertEquals("swapper/0", procname)
        assertEquals(0, tid)
    }

    /**
     * Test the context of the event at 1363700765.559714634.
     */
    @Test
    fun testContext4() {
        val time = startTime + 5 * 5000000000L // 1363700765.559714634
        val event = getEventAt(time)
        val perfPageFault = event.getField("context.perf_page_fault", IntegerValue::class.java)!!.value
        val procname = event.getField("context.procname", StringValue::class.java)!!.value
        val tid = event.getField("context.tid", IntegerValue::class.java)!!.value

        assertEquals(21507, perfPageFault)
        assertEquals("swapper/0", procname)
        assertEquals(0, tid)
    }

    /**
     * Test the context of the last event of the trace.
     */
    @Test
    fun testContextEnd() {
        val lastEvent = getEventAt(endTime)
        val perfPageFault = lastEvent.getField("context.perf_page_fault", IntegerValue::class.java)!!.value
        val procname = lastEvent.getField("context.procname", StringValue::class.java)!!.value
        val tid = lastEvent.getField("context.tid", IntegerValue::class.java)!!.value

        assertEquals(22117, perfPageFault)
        assertEquals("lttng-sessiond", procname)
        assertEquals(1230, tid)
    }

    // ------------------------------------------------------------------------
    // Private stuff
    // ------------------------------------------------------------------------

    private fun getEventAt(timestamp: Long): CtfTraceEvent {
        ETT.trace.iterator().use { iter ->
            while (iter.hasNext()) {
                val event = iter.next()
                if (event.timestamp >= timestamp) {
                    return event
                }
            }
        }
        throw IllegalArgumentException("No event with timestamp $timestamp found.")
    }

}
