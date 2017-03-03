/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace;

import java.nio.file.Path;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;

import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent;
import com.efficios.jabberwocky.ctf.trace.event.ICtfTraceEventFactory;
import com.efficios.jabberwocky.trace.Trace;
import com.efficios.jabberwocky.trace.TraceInitializationException;

public abstract class CtfTrace<E extends CtfTraceEvent> extends  Trace<E> {

    private final Path fTracePath;
    private final CTFTrace fInnerTrace;

    protected CtfTrace(Path tracePath) throws TraceInitializationException {
        CTFTrace trace;
        try {
            trace = new CTFTrace(tracePath.toFile());
        } catch (CTFException e) {
            throw new TraceInitializationException(e);
        }

        fTracePath = tracePath;
        fInnerTrace = trace;
    }

    @Override
    public CtfTraceIterator<E> getIterator() {
        return new CtfTraceIterator<E>(this);
    }

    public final CTFTrace getInnerTrace() {
        return fInnerTrace;
    }

    public abstract ICtfTraceEventFactory<E> getEventFactory();
}
