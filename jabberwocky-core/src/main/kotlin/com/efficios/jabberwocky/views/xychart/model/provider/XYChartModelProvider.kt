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
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.views.xychart.model.render.XYChartRender
import com.efficios.jabberwocky.views.xychart.model.render.XYChartSeries
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import java.util.concurrent.FutureTask

abstract class XYChartModelProvider(val providerName: String) {

    private val traceProjectProperty: ObjectProperty<TraceProject<*, *>?> = SimpleObjectProperty()
    fun traceProjectProperty() = traceProjectProperty
    var traceProject
        get() = traceProjectProperty.get()
        set(value) = traceProjectProperty.set(value)

    private val seriesProviders = mutableListOf<XYChartSeriesProvider>()

    fun registerSeries(seriesProvider: XYChartSeriesProvider) {
        seriesProviders.add(seriesProvider)
    }

    fun removeSeries(seriesProvider: XYChartSeriesProvider) {
        seriesProviders.remove(seriesProvider)
    }

    fun generateSeriesRenders(range: TimeRange, resolution: Long, task: FutureTask<*>? = null): List<XYChartRender> {
        if (resolution <= 0) {
            throw IllegalArgumentException("Resolution parameter must be positive, was $resolution")
        }
        return seriesProviders
                .map { it.generateSeriesRender(range, resolution, task) }
                .toList()
    }
}
