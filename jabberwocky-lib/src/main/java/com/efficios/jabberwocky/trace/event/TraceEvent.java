/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace.event;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.efficios.jabberwocky.trace.event.field.IFieldValue;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

public class TraceEvent implements ITraceEvent {

    private final long fTimestamp;
    private final int fCpu;
    private final String fEventName;
    private final Map<String, IFieldValue> fEventFields;
    private final Map<String, String> fAttributes;

    public TraceEvent(long timestamp,
            int cpu,
            String eventName,
            Map<String, IFieldValue> eventFields,
            @Nullable Map<String, String> attributes) {
        fTimestamp = timestamp;
        fCpu = cpu;
        fEventName = eventName;
        fEventFields = ImmutableMap.copyOf(eventFields);
        fAttributes = (attributes == null ? Collections.EMPTY_MAP : ImmutableMap.copyOf(attributes));
    }

    @Override
    public long getTimestamp() {
        return fTimestamp;
    }

    @Override
    public int getCpu() {
        return fCpu;
    }

    @Override
    public String getEventName() {
        return fEventName;
    }

    @Override
    public Set<String> getFieldNames() {
        return fEventFields.keySet();
    }

    @Override
    public <T extends IFieldValue> @Nullable T getField(String fieldName, Class<T> type) {
        Object value = fEventFields.get(fieldName);
        if (value == null) {
            /*
             * No field with this name found (the map doesn't accept null
             * values)
             */
            return null;
        }
        if (!type.isAssignableFrom(value.getClass())) {
            /* Field exists but is not of the expected type */
            return null;
        }
        @SuppressWarnings("unchecked")
        T ret = (T) value;
        return ret;
    }

    @Override
    public Map<String, String> getAttributes() {
        return fAttributes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fTimestamp, fCpu, fEventName, fEventFields, fAttributes);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TraceEvent other = (TraceEvent) obj;
        return fTimestamp == other.fTimestamp
                && fCpu == other.fCpu
                && Objects.equals(fEventName, other.fEventName)
                && Objects.equals(fEventFields, other.fEventFields)
                && Objects.equals(fAttributes, other.fAttributes);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("timestamp", fTimestamp) //$NON-NLS-1$
                .add("event name", fEventName) //$NON-NLS-1$
                .add("cpu", fCpu) //$NON-NLS-1$
                .add("fields", fEventFields) //$NON-NLS-1$
                .toString();
    }

}
