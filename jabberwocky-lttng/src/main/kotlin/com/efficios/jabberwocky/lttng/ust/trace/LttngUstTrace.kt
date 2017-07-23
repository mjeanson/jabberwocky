/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.ust.trace

import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTraceEventFactory
import com.efficios.jabberwocky.ctf.trace.getTracerMajorVersion
import com.efficios.jabberwocky.ctf.trace.getTracerMinorVersion
import com.efficios.jabberwocky.ctf.trace.getTracerName
import com.efficios.jabberwocky.lttng.ust.trace.layout.*
import com.efficios.jabberwocky.trace.TraceInitializationException
import java.nio.file.Path

class LttngUstTrace(tracePath: Path) : CtfTrace<CtfTraceEvent>(tracePath) {

    init {
        /* Make sure the CTF metadata advertises "domain = ust" */
        val domain = environment["domain"] ?: throw TraceInitializationException()
        if (domain != "\"ust\"") throw TraceInitializationException()
    }

    override val eventFactory = GenericCtfTraceEventFactory(this)

    val ustEventLayout: ILttngUstEventLayout by lazy {
        val defaultLayout = LttngUst20EventLayout.getInstance()
        val tracerName = getTracerName()
        val majorVersion = getTracerMajorVersion() ?: -1
        val minorVersion = getTracerMinorVersion() ?: -1

        val layout: ILttngUstEventLayout = when (tracerName) {
            "lttng-ust" -> when {
                majorVersion >= 2 -> when {
                    minorVersion >= 9 -> LttngUst29EventLayout.getInstance()
                    minorVersion >= 8 -> LttngUst28EventLayout.getInstance()
                    minorVersion >= 7 -> LttngUst27EventLayout.getInstance()
                    else -> LttngUst20EventLayout.getInstance()
                }
                else -> defaultLayout
            }
            else -> defaultLayout
        }
        layout
    }
}