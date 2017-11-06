/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.collection

import com.efficios.jabberwocky.collection.TraceCollection
import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.ctf.trace.ExtractedCtfTestTrace
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.trace.event.FieldValue
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test

class CtfTraceCollectionTest {

    companion object {
        @JvmField
        @ClassRule
        val ETT1 = ExtractedCtfTestTrace(CtfTestTrace.KERNEL)

        @JvmField
        @ClassRule
        val ETT2 = ExtractedCtfTestTrace(CtfTestTrace.TRACE2)

        @JvmField
        @ClassRule
        val ETT3 = ExtractedCtfTestTrace(CtfTestTrace.KERNEL_VM)
    }

    private lateinit var fixture: TraceCollection<CtfTraceEvent, CtfTrace>

    @Before
    fun setup() {
        fixture = TraceCollection(listOf(ETT1.trace, ETT2.trace, ETT3.trace))
    }

    @Test
    fun testEventCount() {
        val expectedCount = CtfTestTrace.KERNEL.nbEvents + CtfTestTrace.TRACE2.nbEvents + CtfTestTrace.KERNEL_VM.nbEvents
        var actualCount = 0
        fixture.iterator().use {
            actualCount = it.asSequence().count()

        }
        assertEquals(expectedCount, actualCount)
    }

    @Test
    fun testSeeking() {
        val targetTimestamp = 1331668247_414253139L
        val targetEvent = CtfTraceEvent(ETT2.trace, targetTimestamp, 0, "exit_syscall",
                mapOf("ret" to FieldValue.IntegerValue(2))
        )
        val nextEvent = CtfTraceEvent(ETT2.trace, 1331668247_414253820, 0, "sys_read",
                mapOf("fd" to FieldValue.IntegerValue(10),
                        "buf" to FieldValue.IntegerValue(0x7FFF6D638FA2, 16),
                        "count" to FieldValue.IntegerValue(8189))
        )
        val prevEvent = CtfTraceEvent(ETT2.trace, 1331668247_414250616, 0, "sys_read",
                mapOf("fd" to FieldValue.IntegerValue(10),
                        "buf" to FieldValue.IntegerValue(0x7FFF6D638FA0, 16),
                        "count" to FieldValue.IntegerValue(8191))
        )

        fixture.iterator().use {
            it.seek(targetTimestamp)
            assertEquals(targetEvent, it.next())
            assertEquals(nextEvent, it.next())

            assertEquals(nextEvent, it.previous())
            assertEquals(targetEvent, it.previous())
            assertEquals(prevEvent, it.previous())
        }
    }

    // TODO More tests, especially with overlapping traces
}
