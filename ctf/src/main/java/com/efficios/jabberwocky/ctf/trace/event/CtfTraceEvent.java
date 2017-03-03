/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace.event;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.efficios.jabberwocky.trace.event.TraceEvent;
import com.efficios.jabberwocky.trace.event.field.IFieldValue;

public class CtfTraceEvent extends TraceEvent {

    public CtfTraceEvent(long timestamp,
            int cpu,
            String eventName,
            Map<String, IFieldValue> eventFields,
            @Nullable Map<String, String> attributes) {
        super(timestamp, cpu, eventName, eventFields, attributes);
    }

}
