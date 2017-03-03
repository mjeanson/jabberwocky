/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace.event.field;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

public abstract class FieldValue implements IFieldValue {

    private final Map<String, String> fAttributes;

    protected FieldValue(@Nullable Map<String, String> attributes) {
        fAttributes = (attributes == null ? Collections.EMPTY_MAP : ImmutableMap.copyOf(attributes));
    }

    @Override
    public Map<String, String> getAttributes() {
        return fAttributes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fAttributes);
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
        FieldValue other = (FieldValue) obj;
        return Objects.equals(fAttributes, other.fAttributes);
    }

    @Override
    public abstract String toString();

}
