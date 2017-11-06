/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace

import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace
import org.junit.Assert.assertEquals
import org.junit.ClassRule
import org.junit.Test

class CtfTraceTest {

    companion object {
        @JvmField
        @ClassRule
        val ETT1 = ExtractedCtfTestTrace(CtfTestTrace.KERNEL)

        @JvmField
        @ClassRule
        val ETT2 = ExtractedCtfTestTrace(CtfTestTrace.TRACE2)
    }

    @Test
    fun testName() {
        assertEquals("kernel", ETT1.trace.name)
        assertEquals("trace2", ETT2.trace.name)
    }
    
}