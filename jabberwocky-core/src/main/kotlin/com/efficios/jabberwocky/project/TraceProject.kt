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
        if (traceCollections.isEmpty()) throw IllegalArgumentException("Project needs at least 1 trace")
    }

    override fun iterator(): ITraceProjectIterator<E> {
        return TraceProjectIterator(this)
    }

    /* The project's start time is the earliest of all its traces's start times */
    override val startTime: Long = traceCollections
                .flatMap { collection -> collection.traces }
                .map { trace -> trace.startTime }
                .min() ?: 0L


    /* The project's end time is the latest of all its traces's end times */
    override val endTime: Long = traceCollections
            .flatMap { collection -> collection.traces }
            .map { trace -> trace.endTime }
            .max() ?: 0L
}