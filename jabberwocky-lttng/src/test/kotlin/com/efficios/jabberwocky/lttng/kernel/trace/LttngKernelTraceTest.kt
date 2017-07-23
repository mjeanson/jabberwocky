/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.trace

import com.efficios.jabberwocky.lttng.kernel.trace.layout.Lttng28EventLayout
import com.efficios.jabberwocky.lttng.kernel.trace.layout.LttngEventLayout
import com.efficios.jabberwocky.lttng.testutils.ExtractedCtfTestTrace
import com.efficios.jabberwocky.trace.TraceInitializationException
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace
import org.junit.ClassRule
import org.junit.Test
import kotlin.test.assertEquals

class LttngKernelTraceTest {

    companion object {
        @JvmField @ClassRule
        val KERNEL_TRACE = ExtractedCtfTestTrace(CtfTestTrace.KERNEL)
        @JvmField @ClassRule
        val KERNEL_TRACE2 = ExtractedCtfTestTrace(CtfTestTrace.MANY_THREADS)
        @JvmField @ClassRule
        val NON_KERNEL_TRACE = ExtractedCtfTestTrace(CtfTestTrace.CYG_PROFILE)
    }

    @Test
    fun testOpeningKernelTrace() {
        val path = KERNEL_TRACE.trace.tracePath
        LttngKernelTrace(path)
    }

    @Test(expected = TraceInitializationException::class)
    fun testOpeningNonKernelTrace() {
        val path = NON_KERNEL_TRACE.trace.tracePath
        LttngKernelTrace(path)
    }

    @Test
    fun testEventLayout() {
        val path1 = KERNEL_TRACE.trace.tracePath
        val trace1 = LttngKernelTrace(path1)
        assertEquals(LttngEventLayout.getInstance(), trace1.kernelEventLayout)

        val path2 = KERNEL_TRACE2.trace.tracePath
        val trace2 = LttngKernelTrace(path2)
        assertEquals(Lttng28EventLayout.getInstance(), trace2.kernelEventLayout)
    }
}