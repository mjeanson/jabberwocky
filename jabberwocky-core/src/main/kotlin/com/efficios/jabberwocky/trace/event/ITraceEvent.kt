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

interface ITraceEvent {

    val trace: Trace<ITraceEvent>

    val timestamp: Long

    val cpu: Int

    val eventName: String

    val fieldNames: Set<String>

    fun <T : FieldValue> getField(fieldName: String, fieldType: Class<T>): T?

    val attributes: Map<String, String>

}
