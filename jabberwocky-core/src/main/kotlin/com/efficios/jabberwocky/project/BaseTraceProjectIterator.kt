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
import com.efficios.jabberwocky.collection.ITraceCollectionIterator
import com.efficios.jabberwocky.utils.SortedCompoundIterator

class BaseTraceProjectIterator<out E : ITraceEvent> (project: TraceProject<E, ITrace<E>>) : SortedCompoundIterator<E, ITraceCollectionIterator<E>>(
        project.traceCollections.map { it.iterator() },
        Comparator.comparingLong { event -> event.timestamp }), TraceProjectIterator<E> {

    override fun close() {
        iterators.forEach { it.close() }
    }

}