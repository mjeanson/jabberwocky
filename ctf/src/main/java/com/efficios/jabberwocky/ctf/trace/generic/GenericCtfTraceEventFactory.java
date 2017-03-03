/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace.generic;

import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;

import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent;
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEventFieldParser;
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceLostEvent;
import com.efficios.jabberwocky.ctf.trace.event.ICtfTraceEventFactory;
import com.efficios.jabberwocky.trace.event.field.IFieldValue;
import com.google.common.collect.ImmutableMap;

public class GenericCtfTraceEventFactory implements ICtfTraceEventFactory<CtfTraceEvent> {

    private static final String UNDERSCORE = "_";
    private static final String CONTEXT_FIELD_PREFIX = "context.";

    private final GenericCtfTrace fTrace;

    public GenericCtfTraceEventFactory(GenericCtfTrace trace) {
        fTrace = trace;
    }

    @Override
    public CtfTraceEvent createEvent(IEventDefinition eventDef) {
        /* lib quirk, eventDef.getTimestamp() actually returns a cycle count... */
        long cycles = eventDef.getTimestamp();
        long ts = fTrace.getInnerTrace().timestampCyclesToNanos(cycles);

        int cpu = eventDef.getCPU();
        String eventName = eventDef.getDeclaration().getName();

        /* Handle the special case of lost events */
        if (eventName.equals(CTFStrings.LOST_EVENT_NAME)) {
            IDefinition nbLostEventsDef = eventDef.getFields().getDefinition(CTFStrings.LOST_EVENTS_FIELD);
            IDefinition durationDef = eventDef.getFields().getDefinition(CTFStrings.LOST_EVENTS_DURATION);
            long nbLostEvents = ((IntegerDefinition) nbLostEventsDef).getValue();
            long duration = ((IntegerDefinition) durationDef).getValue();
            long endTime = ts + duration;

            return new CtfTraceLostEvent(ts, endTime, cpu, eventName, nbLostEvents);
        }

        // TODO Rest could be lazy-loaded at some point?

        ImmutableMap.Builder<String, IFieldValue> fields = ImmutableMap.builder();

        /* Parse the event fields (payload) */
        ICompositeDefinition fieldsDef = eventDef.getFields();
        if (fieldsDef != null && fieldsDef.getFieldNames() != null) {
            for (String fieldName : fieldsDef.getFieldNames()) {
                /* Strip the underscore from the field name if there is one */
                String usedFieldName = (fieldName.startsWith(UNDERSCORE) ? fieldName.substring(1) : fieldName);
                fields.put(usedFieldName, CtfTraceEventFieldParser.parseField(fieldsDef.getDefinition(fieldName)));
            }
        }

        /* Add context information */
        ICompositeDefinition contextDef = eventDef.getContext();
        if (contextDef != null) {
            for (String contextName : contextDef.getFieldNames()) {
                /*
                 * Prefix the name we'll use with "context.", and remove
                 * leading underscores if needed.
                 */
                String usedContextName = (contextName.startsWith(UNDERSCORE)
                        ? CONTEXT_FIELD_PREFIX + contextName.substring(1)
                        : CONTEXT_FIELD_PREFIX + contextName);
                fields.put(usedContextName, CtfTraceEventFieldParser.parseField(contextDef.getDefinition(contextName)));
            }
        }

        /* No custom attributes at the moment */
        return new CtfTraceEvent(ts, cpu, eventName, fields.build(), null);
    }

}
