package com.efficios.jabberwocky.trace

import com.efficios.jabberwocky.trace.event.TraceEvent

abstract class Trace<out E : TraceEvent> : ITrace<E> {

    /* Lazy-load the start time by reading the timestamp of the first event. */
    override val startTime: Long by lazy {
        var startTime: Long = 0L
        iterator().use { iter ->
            if (iter.hasNext()) {
                startTime = iter.next().timestamp
            }
        }
        startTime
    }

}
