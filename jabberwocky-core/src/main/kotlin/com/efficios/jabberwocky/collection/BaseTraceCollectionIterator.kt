/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.collection

import com.efficios.jabberwocky.trace.ITrace
import com.efficios.jabberwocky.trace.ITraceIterator
import com.efficios.jabberwocky.trace.event.ITraceEvent
import com.efficios.jabberwocky.utils.SortedCompoundIterator
import java.util.*

class BaseTraceCollectionIterator<E : ITraceEvent> (traceCollection: TraceCollection<E, ITrace<E>>) : SortedCompoundIterator<E, ITraceIterator<E>>(
        traceCollection.traces.map { it.iterator() },
        Comparator.comparingLong { event -> event.timestamp }), TraceCollectionIterator<E> {

    override fun close() {
        iterators.forEach { it.close() }
    }

}
