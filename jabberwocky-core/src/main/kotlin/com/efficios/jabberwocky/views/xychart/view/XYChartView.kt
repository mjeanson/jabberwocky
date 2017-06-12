/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.xychart.view

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.views.xychart.control.XYChartControl

interface XYChartView {

    val control: XYChartControl

    val viewContext get() = control.viewContext

    fun dispose()
    fun clear()
    fun seekVisibleRange(newVisibleRange: TimeRange)
    fun drawSelection(selectionRange: TimeRange)
}
