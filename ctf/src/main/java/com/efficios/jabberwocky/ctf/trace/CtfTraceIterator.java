/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace;

import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;

import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent;
import com.efficios.jabberwocky.trace.ITraceIterator;

public class CtfTraceIterator<E extends CtfTraceEvent> implements ITraceIterator<E> {

    private final CtfTrace<E> fOriginTrace;
    private final CTFTraceReader fTraceReader;

    private @Nullable IEventDefinition fCurrentEventDef;

    public CtfTraceIterator(CtfTrace<E> originTrace) {
        CTFTraceReader reader;
        try {
            reader = new CTFTraceReader(originTrace.getInnerTrace());
        } catch (CTFException e) {
            /*
             * If the CtfTrace was initialized successfully, creating an
             * iterator should not fail.
             */
            throw new IllegalStateException(e);
        }
        fOriginTrace = originTrace;
        fTraceReader = reader;
        fCurrentEventDef = reader.getCurrentEventDef();
    }

    @Override
    public boolean hasNext() {
        return (fCurrentEventDef != null);
    }

    @Override
    public E next() {
        IEventDefinition currentEventDef = fCurrentEventDef;
        if (currentEventDef == null) {
            throw new NoSuchElementException();
        }

        /* Wrap the current event into a JW event */
        E event = fOriginTrace.getEventFactory().createEvent(currentEventDef);

        /* Prepare the "next next" event */
        try {
            fTraceReader.advance();
            fCurrentEventDef = fTraceReader.getCurrentEventDef();
        } catch (CTFException e) {
            /* Shouldn't happen if we did the other checks correctly */
            throw new IllegalStateException(e);
        }
        return event;
    }

    @Override
    public void close() {
        fTraceReader.close();
    }

}
