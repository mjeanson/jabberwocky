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
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTraceEventFactory
import com.efficios.jabberwocky.ctf.trace.getTracerMajorVersion
import com.efficios.jabberwocky.ctf.trace.getTracerMinorVersion
import com.efficios.jabberwocky.ctf.trace.getTracerName
import com.efficios.jabberwocky.lttng.kernel.trace.layout.*
import com.efficios.jabberwocky.trace.TraceInitializationException
import java.nio.file.Path

class LttngKernelTrace(tracePath: Path) : CtfTrace<CtfTraceEvent>(tracePath) {

    init {
        /* Make sure the CTF metadata advertises "domain = kernel" */
        val domain = environment["domain"] ?: throw TraceInitializationException()
        if (domain != "\"kernel\"") throw TraceInitializationException()
    }

    override val eventFactory = GenericCtfTraceEventFactory(this)

    val kernelEventLayout: ILttngKernelEventLayout by lazy {
        val defaultLayout = LttngEventLayout.getInstance()
        val tracerName = getTracerName()
        val majorVersion = getTracerMajorVersion() ?: -1
        val minorVersion = getTracerMinorVersion() ?: -1

        val layout: ILttngKernelEventLayout = when (tracerName) {
            "perf" -> PerfEventLayout.getInstance()
            "lttng-modules" -> when {
                majorVersion >= 2 -> when {
                    minorVersion >= 9 -> Lttng29EventLayout.getInstance()
                    minorVersion >= 8 -> Lttng28EventLayout.getInstance()
                    minorVersion >= 7 -> Lttng27EventLayout.getInstance()
                    minorVersion >= 6 -> Lttng26EventLayout.getInstance()
                    else -> LttngEventLayout.getInstance()
                }
                else -> defaultLayout
            }
            else -> defaultLayout
        }
        layout
    }
}