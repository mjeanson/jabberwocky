/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.project

import com.efficios.jabberwocky.trace.ITrace
import com.efficios.jabberwocky.trace.event.ITraceEvent
import com.efficios.jabberwocky.collection.TraceCollection
import java.nio.file.Files
import java.nio.file.Path

class TraceProject<out E : ITraceEvent, out T : ITrace<E>> (override val name: String,
                                                            override val directory: Path,
                                                            override val traceCollections: Collection<TraceCollection<E, T>>) : ITraceProject<E, T> {

    init {
        if (!Files.isReadable(directory) || !Files.isWritable(directory)) throw IllegalArgumentException("Invalid project directory")
    }

    override fun iterator(): ITraceProjectIterator<E> {
        return TraceProjectIterator(this)
    }

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