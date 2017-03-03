/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace.generic;

import java.nio.file.Path;

import com.efficios.jabberwocky.ctf.trace.CtfTrace;
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent;
import com.efficios.jabberwocky.trace.TraceInitializationException;

public class GenericCtfTrace extends CtfTrace<CtfTraceEvent> {

    private final GenericCtfTraceEventFactory fEventFactory;

    public GenericCtfTrace(Path tracePath) throws TraceInitializationException {
        super(tracePath);
        fEventFactory = new GenericCtfTraceEventFactory(this);
    }

    @Override
    public GenericCtfTraceIterator getIterator() {
        return new GenericCtfTraceIterator(this);
    }

    @Override
    public GenericCtfTraceEventFactory getEventFactory() {
        return fEventFactory;
    }

}
