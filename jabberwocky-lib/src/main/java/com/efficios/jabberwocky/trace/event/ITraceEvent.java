/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace.event;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.efficios.jabberwocky.trace.event.field.IFieldValue;

public interface ITraceEvent {

    long getTimestamp();

    int getCpu();

    String getEventName();

    Set<String> getFieldNames();

    <T extends IFieldValue> @Nullable T getField(String fieldName, Class<T> fieldType);

    Map<String, String> getAttributes();

}
