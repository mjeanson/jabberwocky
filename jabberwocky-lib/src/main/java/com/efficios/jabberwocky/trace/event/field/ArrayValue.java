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

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

public class ArrayValue<T extends IFieldValue> extends FieldValue {

    private final T[] fElements;

    public ArrayValue(T[] elements, @Nullable Map<String, String> attributes) {
        super(attributes);
        fElements = elements;
    }

    @Override
    public Type getType() {
        return Type.ARRAY;
    }

    public T getElement(int index) {
        return fElements[index];
    }

    public int size() {
        return fElements.length;
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
        ArrayValue<?> other = requireNonNull((ArrayValue<?>) obj);
        return Arrays.deepEquals(fElements, other.fElements);
    }

    @Override
    public String toString() {
        return Arrays.toString(fElements);
    }

}
