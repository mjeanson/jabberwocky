/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.ust.trace

import com.efficios.jabberwocky.lttng.testutils.ExtractedCtfTestTrace
import com.efficios.jabberwocky.trace.TraceInitializationException
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace
import org.junit.ClassRule
import org.junit.Test

class LttngUstTraceTest {

    companion object {
        @JvmField @ClassRule
        val KERNEL_TRACE = ExtractedCtfTestTrace(CtfTestTrace.KERNEL)
        @JvmField @ClassRule
        val UST_TRACE = ExtractedCtfTestTrace(CtfTestTrace.CYG_PROFILE)
    }

    @Test
    fun testOpeningKernelTrace() {
        val path = UST_TRACE.trace.tracePath
        LttngUstTrace(path)
    }

    @Test(expected = TraceInitializationException::class)
    fun testOpeningNonKernelTrace() {
        val path = KERNEL_TRACE.trace.tracePath
        LttngUstTrace(path)
    }
}