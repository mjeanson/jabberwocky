/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.trace

import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.lttng.kernel.trace.layout.Lttng28EventLayout
import com.efficios.jabberwocky.lttng.kernel.trace.layout.LttngEventLayout
import com.efficios.jabberwocky.lttng.testutils.ExtractedCtfTestTrace
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.ClassRule
import org.junit.Test
import org.lttng.scope.ttt.ctf.CtfTestTrace
import kotlin.test.assertEquals

class LttngKernelTraceUtilsTest {

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
        val trace = CtfTrace(path)
        assertTrue(trace.isKernelTrace())
    }

    @Test
    fun testOpeningNonKernelTrace() {
        val path = NON_KERNEL_TRACE.trace.tracePath
        val trace = CtfTrace(path)
        assertFalse(trace.isKernelTrace())
    }

    @Test
    fun testEventLayout() {
        assertEquals(LttngEventLayout.getInstance(), KERNEL_TRACE.trace.getKernelEventLayout())
        assertEquals(Lttng28EventLayout.getInstance(), KERNEL_TRACE2.trace.getKernelEventLayout())
    }
}