/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.xychart.model.provider

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.views.xychart.model.render.XYChartRender
import com.efficios.jabberwocky.views.xychart.model.render.XYChartSeries
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import java.util.concurrent.FutureTask

abstract class XYChartSeriesProvider(val series: XYChartSeries) {

    /** Indicate if this series is enabled or not. */
    private val enabledProperty: BooleanProperty = SimpleBooleanProperty(true)
    fun enabledProperty() = enabledProperty
    var enabled
        get() = enabledProperty.get()
        set(value) = enabledProperty.set(value)

    abstract fun generateSeriesRender(range: TimeRange, resolution: Long, task: FutureTask<*>?): XYChartRender

}
