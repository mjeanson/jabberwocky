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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public class StringValue extends FieldValue {

    private final String fValue;

    public StringValue(String value, @Nullable Map<String, String> attributes) {
        super(attributes);
        fValue = value;
    }

    @Override
    public @NonNull Type getType() {
        return Type.STRING;
    }

    public String getValue() {
        return fValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fValue);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        StringValue other = requireNonNull((StringValue) obj);
        return Objects.equals(fValue, other.fValue);
    }

    @Override
    public String toString() {
        return fValue;
    }

}