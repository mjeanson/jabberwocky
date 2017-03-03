/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace.event.field;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

public class StructValue extends FieldValue {

    private final Map<String, IFieldValue> fElements;

    public StructValue(Map<String, IFieldValue> elements, @Nullable Map<String, String> attributes) {
        super(attributes);
        fElements = ImmutableMap.copyOf(elements);
    }

    @Override
    public @NonNull Type getType() {
        return Type.STRUCTURE;
    }

    public Set<String> getFieldNames() {
        return fElements.keySet();
    }

    public <T extends IFieldValue> @Nullable T getField(String fieldName, Class<T> fieldType) {
        Object value = fElements.get(fieldName);
        if (value == null) {
            return null;
        }
        if (!fieldType.isAssignableFrom(value.getClass())) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T ret = (T) value;
        return ret;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fElements);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        StructValue other = requireNonNull((StructValue) obj);
        return Objects.equals(fElements, other.fElements);
    }

    @Override
    public String toString() {
        return fElements.toString();
    }

}
