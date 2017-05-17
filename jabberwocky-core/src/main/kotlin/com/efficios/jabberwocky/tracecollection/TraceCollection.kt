/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.tracecollection

import com.efficios.jabberwocky.trace.ITrace
import com.efficios.jabberwocky.trace.event.ITraceEvent
import com.google.common.collect.ImmutableSet

class TraceCollection<out E : ITraceEvent>(override val traces: Collection<ITrace<E>>) : ITraceCollection<E> {

    override fun iterator(): ITraceCollectionIterator<E> {
        return TraceCollectionIterator(this)
    }

}