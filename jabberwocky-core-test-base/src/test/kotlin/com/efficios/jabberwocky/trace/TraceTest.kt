/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace

import com.efficios.jabberwocky.trace.event.TraceEvent
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class TraceTest(private val testTrace: Trace<TraceEvent>,
                private val expectedStart: Long,
                private val expectedEnd: Long) {

    companion object {
        @Parameterized.Parameters(name = "{index}: {0}")
        @JvmStatic
        fun getTestTraces(): Iterable<Array<Any>> {
            return listOf(
                    arrayOf<Any>(TraceStubs.TraceStub1(), 2L, 10L),
                    arrayOf<Any>(TraceStubs.TraceStub2(), 4L, 8L)
            )
        }
    }

    @Test
    fun testStartTime() {
        assertEquals(expectedStart, testTrace.startTime)
    }

    @Test
    fun testEndTime() {
        assertEquals(expectedEnd, testTrace.endTime)
    }
}