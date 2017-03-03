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

public class IntegerValue extends FieldValue {

    private static final int DEFAULT_BASE = 10;

    private final long fValue;
    private final int fBase;

    public IntegerValue(long value, @Nullable Integer base, @Nullable Map<String, String> attributes) {
        super(attributes);
        fValue = value;
        fBase = (base == null ? DEFAULT_BASE : base.intValue());
    }

    @Override
    public Type getType() {
        return Type.INTEGER;
    }

    public long getValue() {
        return fValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fValue, fBase);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        IntegerValue other = requireNonNull((IntegerValue) obj);
        return fValue == other.fValue
                && fBase == other.fBase;
    }

    @Override
    public String toString() {
        if (fBase == 16) {
            return "0x" + Long.toHexString(fValue); //$NON-NLS-1$
        }
        return String.valueOf(fValue);
    }

}
