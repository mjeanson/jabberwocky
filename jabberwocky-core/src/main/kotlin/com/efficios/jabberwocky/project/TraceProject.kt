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
import com.efficios.jabberwocky.tracecollection.TraceCollection

class TraceProject<out E : ITraceEvent, out T : ITrace<E>> (override val traceCollections: Collection<TraceCollection<E, T>>) : ITraceProject<E, T> {

    override fun iterator(): ITraceProjectIterator<E> {
        return TraceProjectIterator(this)
    }

}