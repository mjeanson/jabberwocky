/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace

import com.efficios.jabberwocky.trace.event.BaseTraceEvent
import com.efficios.jabberwocky.trace.event.TraceEvent

internal class TraceStubs {

    companion object {
        const val EVENT_NAME_A = "EventA"
        const val EVENT_NAME_B = "EventB"
        const val EVENT_NAME_C = "EventC"
    }

    private class TraceStubIterator(private val trace: TraceStubBase) : TraceIterator<TraceEvent> {

        private val iterator: Iterator<TraceEvent> = trace.events.iterator()

        private var nbRead = 0

        override fun hasNext() = iterator.hasNext()

        override fun next(): TraceEvent {
            nbRead++
            return iterator.next()
        }

        override fun close() {}

        override fun copy(): TraceIterator<TraceEvent> {
            /* Start from the beginning and read the same amount of events that were read. */
            val newIter = TraceStubIterator(trace)
            repeat(nbRead, { newIter.next() })
            return newIter
        }
    }


    abstract class TraceStubBase() : Trace<TraceEvent>() {

        internal abstract val events: List<TraceEvent>

        final override fun iterator(): TraceIterator<TraceEvent> {
            return TraceStubIterator(this)
        }
    }

    class TraceStub1 : TraceStubBase() {

        override val events = listOf(
                BaseTraceEvent(this, 2, 0, EVENT_NAME_A, emptyMap(), null),
                BaseTraceEvent(this, 5, 0, EVENT_NAME_B, emptyMap(), null),
                BaseTraceEvent(this, 10, 1, EVENT_NAME_C, emptyMap(), null))
    }

    class TraceStub2 : TraceStubBase() {

        override val events = listOf(
                BaseTraceEvent(this, 4, 1, EVENT_NAME_B, emptyMap(), null),
                BaseTraceEvent(this, 6, 0, EVENT_NAME_B, emptyMap(), null),
                BaseTraceEvent(this, 8, 1, EVENT_NAME_A, emptyMap(), null))

    }

    /** Trace with identical events going from ts 100 to 200, every 2 units */
    class TraceStub3 : TraceStubBase() {
        override val events = (100L..200L step 2).map { BaseTraceEvent(this, it, 0, EVENT_NAME_A, emptyMap(), null) }
    }
}