/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace

import java.util.NoSuchElementException

import org.eclipse.tracecompass.ctf.core.CTFException
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader

import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.trace.TraceIterator

open class CtfTraceIterator<out E : CtfTraceEvent>(private val originTrace: CtfTrace<E>) : TraceIterator<E> {

    private val traceReader: CTFTraceReader = try {
        CTFTraceReader(originTrace.innerTrace)
    } catch (e: CTFException) {
        /*
         * If the CtfTrace was initialized successfully, creating an
         * iterator should not fail.
         */
        throw IllegalStateException(e)
    }

    private var currentEventDef: IEventDefinition? = traceReader.currentEventDef

    override fun hasNext(): Boolean = (currentEventDef != null)

    override fun next(): E {
        val currentEventDef = currentEventDef ?: throw NoSuchElementException()

        /* Wrap the current event into a JW event */
        val event = originTrace.eventFactory.createEvent(currentEventDef)

        /* Prepare the "next next" event */
        try {
            traceReader.advance()
            this.currentEventDef = traceReader.getCurrentEventDef()
        } catch (e: CTFException) {
            /* Shouldn't happen if we did the other checks correctly */
            throw IllegalStateException(e)
        }

        return event
    }

    override fun close() {
        traceReader.close()
    }

}
