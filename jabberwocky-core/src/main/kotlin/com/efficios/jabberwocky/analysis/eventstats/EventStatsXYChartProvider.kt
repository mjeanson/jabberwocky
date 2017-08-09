/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.analysis.eventstats

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.views.common.FlatUIColors
import com.efficios.jabberwocky.views.xychart.model.provider.statesystem.StateSystemXYChartProvider
import com.efficios.jabberwocky.views.xychart.model.render.XYChartRender
import com.efficios.jabberwocky.views.xychart.model.render.XYChartSeries
import java.util.concurrent.FutureTask
import javax.management.AttributeNotFoundException

class EventStatsXYChartProvider: StateSystemXYChartProvider(NAME, SERIES, EventStatsAnalysis) {

    companion object {
        private const val NAME = "Event count"
        private val SERIES = listOf(
                XYChartSeries("Events total", FlatUIColors.DARK_BLUE, XYChartSeries.LineStyle.INTEGRAL)
        )
    }

    override fun generateRender(series: XYChartSeries, range: TimeRange, resolution: Long, task: FutureTask<*>?): XYChartRender {
        val ss = stateSystem
        if (ss == null || (task != null && task.isCancelled)) {
            return XYChartRender.EMPTY_RENDER
        }

        when (series) {
            /* Only one available series for now */
            SERIES.single() -> {
                val quark: Int = try {
                    ss.getQuarkAbsolute(EventStatsAnalysis.TOTAL_ATTRIBUTE)
                } catch (e: AttributeNotFoundException) {
                    return XYChartRender.EMPTY_RENDER
                }

                /* Map each resolution point to the amount of events seen so far */
                val eventCounts = (range.startTime until range.endTime step resolution).plus(range.endTime)
                        .map { ts ->
                            val sv = ss.querySingleState(ts, quark).stateValue
                            val count = if (sv.isNull) 0L else sv.unboxInt().toLong()
                            /* Return a Pair<ts, count> */
                            ts to count
                        }

                /* Compute the number of events in each "bucket" */
                // FIXME Make this loop more "functional" ?
                val bucketCounts = mutableListOf<Pair<Long, Long>>() // Pair<ts, count>
                for (i in 1 until eventCounts.size) {
                    val ts = listOf(eventCounts[i - 1].first, eventCounts[i].first).average().toLong()
                    val count = eventCounts[i].second - eventCounts[i - 1].second
                    bucketCounts.add(ts to count)
                }

                val dataPoints = bucketCounts.map { XYChartRender.DataPoint(it.first, it.second) }
                return XYChartRender(series, range, dataPoints)
            }

            else -> throw IllegalArgumentException()
        }
    }
}