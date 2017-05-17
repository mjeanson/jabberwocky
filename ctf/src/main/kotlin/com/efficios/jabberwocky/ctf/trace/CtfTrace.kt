/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace

import java.nio.file.Path

import org.eclipse.tracecompass.ctf.core.CTFException
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace

import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.ctf.trace.event.ICtfTraceEventFactory
import com.efficios.jabberwocky.trace.Trace
import com.efficios.jabberwocky.trace.TraceInitializationException

abstract class CtfTrace<E : CtfTraceEvent>(tracePath: Path) : Trace<E>() {

    internal val innerTrace: CTFTrace

    init {
        val trace: CTFTrace
        try {
            trace = CTFTrace(tracePath.toFile())
        } catch (e: CTFException) {
            throw TraceInitializationException(e)
        }

        innerTrace = trace
    }

    override fun iterator(): CtfTraceIterator<E> {
        return CtfTraceIterator<E>(this)
    }

    abstract val eventFactory: ICtfTraceEventFactory<E>
}
