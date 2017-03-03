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

import org.eclipse.jdt.annotation.Nullable;

public class EnumValue extends FieldValue {

    private final String fStringValue;
    private final long fLongValue;

    public EnumValue(String stringValue, long longValue, @Nullable Map<String, String> attributes) {
        super(attributes);
        fStringValue = stringValue;
        fLongValue = longValue;
    }

    @Override
    public Type getType() {
        return Type.ENUMERATION;
    }

    public String getStringValue() {
        return fStringValue;
    }

    public long getLongValue() {
        return fLongValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fStringValue, fLongValue);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        EnumValue other = requireNonNull((EnumValue) obj);
        return Objects.equals(fStringValue, other.fStringValue)
                && fLongValue == other.fLongValue;
    }

    @Override
    public String toString() {
        return fStringValue + '(' + String.valueOf(fLongValue) + ')';
    }
}
