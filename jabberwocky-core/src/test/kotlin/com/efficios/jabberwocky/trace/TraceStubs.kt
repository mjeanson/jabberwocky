/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace

import com.efficios.jabberwocky.trace.event.TraceEvent

class TraceStubs {

    companion object {
        const val EVENT_NAME_A = "EventA"
        const val EVENT_NAME_B = "EventB"
        const val EVENT_NAME_C = "EventC"
    }

    private class TraceStubIterator(private val iterator: Iterator<TraceEvent>) : ITraceIterator<TraceEvent> {
        override fun hasNext() = iterator.hasNext()
        override fun next() = iterator.next()
        override fun close() {}
    }

    abstract class TraceStubBase(private val events: List<TraceEvent>) : Trace<TraceEvent>() {
        final override fun iterator(): ITraceIterator<TraceEvent> {
            return TraceStubIterator(events.iterator())
        }
    }

    class TraceStub1 : TraceStubBase(listOf(
            TraceEvent( 2, 0, EVENT_NAME_A, emptyMap(), null),
            TraceEvent( 5, 0, EVENT_NAME_B, emptyMap(), null),
            TraceEvent(10, 1, EVENT_NAME_C, emptyMap(), null)
    ))

    class TraceStub2 : TraceStubBase(listOf(
            TraceEvent( 4, 1, EVENT_NAME_B, emptyMap(), null),
            TraceEvent( 6, 0, EVENT_NAME_B, emptyMap(), null),
            TraceEvent( 8, 1, EVENT_NAME_A, emptyMap(), null)
    ))
}