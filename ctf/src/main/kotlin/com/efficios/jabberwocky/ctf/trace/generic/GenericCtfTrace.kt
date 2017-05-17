/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace.generic

import java.nio.file.Path

import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.trace.TraceInitializationException

class GenericCtfTrace(tracePath: Path) : CtfTrace<CtfTraceEvent>(tracePath) {

    override val eventFactory = GenericCtfTraceEventFactory(this)

    override fun iterator(): GenericCtfTraceIterator {
        return GenericCtfTraceIterator(this)
    }

}
