/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.tracecollection

import java.util.Comparator

import com.efficios.jabberwocky.trace.ITraceIterator
import com.efficios.jabberwocky.trace.event.ITraceEvent
import com.efficios.jabberwocky.utils.SortedCompoundIterator
import com.google.common.collect.ImmutableList
import java.util.function.Consumer

class TraceCollectionIterator<E : ITraceEvent> (traceCollection: ITraceCollection<E>) : SortedCompoundIterator<E, ITraceIterator<E>>(
        traceCollection.traces.map { it.iterator() },
        Comparator.comparingLong { event -> event.timestamp }), ITraceCollectionIterator<E> {

    override fun close() {
        iterators.forEach({ it.close() })
    }

}
