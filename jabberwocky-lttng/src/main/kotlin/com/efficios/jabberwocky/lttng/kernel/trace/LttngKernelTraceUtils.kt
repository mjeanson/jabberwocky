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
import com.efficios.jabberwocky.ctf.trace.getTracerMajorVersion
import com.efficios.jabberwocky.ctf.trace.getTracerMinorVersion
import com.efficios.jabberwocky.ctf.trace.getTracerName
import com.efficios.jabberwocky.lttng.kernel.trace.layout.*

fun CtfTrace.isKernelTrace() =
        /* Check if the CTF metadata advertises "domain = kernel" */
        environment["domain"] == "\"kernel\""

/**
 * Retrieve the kernel event layout applicable to this trace,
 * or null if this does not look like a kernel trace.
 */
fun CtfTrace.getKernelEventLayout(): ILttngKernelEventLayout? {
    if (!isKernelTrace()) return null

    val defaultLayout = LttngEventLayout.getInstance()
    val tracerName = getTracerName()
    val majorVersion = getTracerMajorVersion() ?: -1
    val minorVersion = getTracerMinorVersion() ?: -1

    return when (tracerName) {
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
}