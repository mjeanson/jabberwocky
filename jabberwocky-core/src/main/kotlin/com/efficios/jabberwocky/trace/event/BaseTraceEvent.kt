/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace.event

import com.efficios.jabberwocky.trace.Trace
import com.google.common.base.MoreObjects
import com.google.common.collect.ImmutableMap
import java.text.NumberFormat
import java.util.*

open class BaseTraceEvent(@Transient override val trace: Trace<TraceEvent>,
                      override val timestamp: Long,
                      override val cpu: Int,
                      override val eventName: String,
                      eventFields: Map<String, FieldValue>,
                      attributes: Map<String, String>? = null) : TraceEvent {

    private val fEventFields: Map<String, FieldValue> = ImmutableMap.copyOf(eventFields)

    final override val attributes: Map<String, String> = if (attributes == null) Collections.emptyMap() else ImmutableMap.copyOf(attributes)

    final override val fieldNames = fEventFields.keys

    override fun <T : FieldValue> getField(fieldName: String, fieldType: Class<T>): T? {
        val value = fEventFields[fieldName] ?: /*
             * No field with this name found (the map doesn't accept null
             * values)
             */
                return null
        if (!fieldType.isAssignableFrom(value.javaClass)) {
            /* Field exists but is not of the expected type */
            return null
        }
        val ret = value as T
        return ret
    }

    override fun hashCode() = Objects.hash(timestamp, cpu, eventName, fEventFields, attributes)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as BaseTraceEvent

        if (timestamp != other.timestamp) return false
        if (cpu != other.cpu) return false
        if (eventName != other.eventName) return false
        if (fEventFields != other.fEventFields) return false
        if (attributes != other.attributes) return false

        return true
    }

    override fun toString(): String {
        return MoreObjects.toStringHelper(this)
                .add("timestamp", NumberFormat.getInstance().format(timestamp)) //$NON-NLS-1$
                .add("event name", eventName) //$NON-NLS-1$
                .add("cpu", cpu) //$NON-NLS-1$
                .add("fields", fEventFields) //$NON-NLS-1$
                .toString()
    }

}
