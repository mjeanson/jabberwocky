/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.provider.statesystem

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval
import com.efficios.jabberwocky.analysis.statesystem.StateSystemAnalysis
import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.views.timegraph.model.provider.states.TimeGraphModelStateProvider
import com.efficios.jabberwocky.views.timegraph.model.render.StateDefinition
import com.efficios.jabberwocky.views.timegraph.model.render.states.MultiStateInterval
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateInterval
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateRender
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender
import java.util.*
import java.util.concurrent.FutureTask

/**
 * Basic implementation of a {@link TimeGraphModelStateProvider} backed by a state
 * system.
 *
 * @author Alexandre Montplaisir
 */
abstract class StateSystemModelStateProvider(stateDefinitions: List<StateDefinition>,
                                             stateSystemAnalysis: StateSystemAnalysis) : TimeGraphModelStateProvider(stateDefinitions) {

    /**
     * This state system here is not necessarily the same as the one in the
     * {@link StateSystemModelProvider}!
     */
    @Transient
    private var stateSystem: ITmfStateSystem? = null

    init {
        /*
         * Change listener which will take care of keeping the target state
         * system up to date.
         */
        traceProjectProperty().addListener { _, _, newValue ->
            val project = newValue
            stateSystem = if (project != null
                    && stateSystemAnalysis.appliesTo(project)
                    && stateSystemAnalysis.canExecute(project)) {
                // TODO Cache this?
                stateSystemAnalysis.execute(project, null, null)
            } else {
                null
            }
        }

    }

    /**
     * Define how this state provider generates model intervals.
     *
     * @param ss
     *            The target state system
     * @param treeElem
     *            The timegraph tree element (FIXME Required because of the state
     *            interval's constructor, otherwise the subclasses should only need
     *            the quark)
     * @param interval
     *            The source interval
     * @return The timegraph model interval object, you can use
     *         {@link BasicTimeGraphStateInterval} for a simple implementation.
     */
    protected abstract fun createInterval(ss: ITmfStateSystem,
                                          treeElem: StateSystemTimeGraphTreeElement,
                                          interval: ITmfStateInterval): TimeGraphStateInterval

    // ------------------------------------------------------------------------
    // Render generation methods
    // ------------------------------------------------------------------------

    override fun getStateRender(treeElement: TimeGraphTreeElement,
                                timeRange: TimeRange,
                                resolution: Long,
                                task: FutureTask<*>?): TimeGraphStateRender {

        /* "Title" entries should be ignored */
        if (treeElement !is StateSystemTimeGraphTreeElement) {
            return TimeGraphStateRender.EMPTY_RENDER
        }

        val ss = stateSystem
        if (ss == null || (task != null && task.isCancelled)) {
            return TimeGraphStateRender.EMPTY_RENDER
        }

        val modelIntervals = queryHistoryRange(ss, treeElement, timeRange, resolution, task)
        /* Fill the row with multi-states */
        val filledIntervals = fillWithMultiStates(timeRange, treeElement, modelIntervals)
        return TimeGraphStateRender(timeRange, treeElement, filledIntervals)
    }

    override fun getStateRenders(treeElements: Set<TimeGraphTreeElement>,
                                 timeRange: TimeRange,
                                 resolution: Long,
                                 task: FutureTask<*>?): Map<TimeGraphTreeElement, TimeGraphStateRender> {

        return treeElements.associate { Pair(it, getStateRender(it, timeRange, resolution, task)) }
    }

    override fun getAllStateRenders(treeRender: TimeGraphTreeRender,
                                    timeRange: TimeRange,
                                    resolution: Long,
                                    task: FutureTask<*>?): List<TimeGraphStateRender> {

        /* Ensure the returned list has the same order as the .allTreeElements */
        val rendersMap = getStateRenders(treeRender.allTreeElements.toSet(), timeRange, resolution, task)
        return treeRender.allTreeElements.map { rendersMap[it]!! }
    }

    private fun queryHistoryRange(ss: ITmfStateSystem,
                                  treeElem: StateSystemTimeGraphTreeElement,
                                  timeRange: TimeRange,
                                  resolution: Long,
                                  task: FutureTask<*>?): List<TimeGraphStateInterval> {

        val t1 = timeRange.startTime
        val t2 = timeRange.endTime

        /* Validate the parameters. */
        if (t2 < t1
                || resolution <= 0
                || t1 < ss.startTime
                || t2 > ss.currentEndTime) {
            throw IllegalArgumentException(ss.ssid + " Start:" + t1 + ", End:" + t2 + ", Resolution:" + resolution)
        }

        val modelIntervals = LinkedList<TimeGraphStateInterval>()
        val attributeQuark = treeElem.sourceQuark
        var lastAddedInterval: ITmfStateInterval? = null

        /*
         * First, iterate over the "resolution points" and keep all matching
         * state intervals.
         */
        var ts = t1 - resolution // incremented to t1 at first loop
        while (ts <= t2 - resolution) {
            ts += resolution
            /*
             * Skip queries if the corresponding interval was already included
             */
            if (lastAddedInterval != null && lastAddedInterval.endTime >= ts) {
                val nextTOffset = roundToClosestHigherMultiple(lastAddedInterval.endTime - t1, resolution)
                val nextTs = t1 + nextTOffset
                if (nextTs == ts) {
                    /*
                     * The end time of the last interval happened to be exactly
                     * equal to the next resolution point. We will go to the
                     * resolution point after that then.
                     */
                    ts = nextTs
                } else {
                    /* 'ts' will get incremented at next loop */
                    ts = nextTs - resolution
                }
                continue
            }

            val stateSystemInterval = ss.querySingleState(ts, attributeQuark)

            /*
             * Only pick the interval if it fills the current resolution range,
             * from 'ts' to 'ts + resolution' (or 'ts2').
             */
            val ts2 = ts + resolution
            if (stateSystemInterval.startTime <= ts && stateSystemInterval.endTime >= ts2) {
                val interval = createInterval(ss, treeElem, stateSystemInterval)
                modelIntervals.add(interval)
                lastAddedInterval = stateSystemInterval
            }
        }

        /*
         * For the very last interval, we'll use ['tEnd - resolution', 'tEnd']
         * as a range condition instead.
         */
        ts = Math.max(t1, t2 - resolution)
        val ts2 = t2
        if (lastAddedInterval != null && lastAddedInterval.endTime >= ts) {
            /* Interval already included */
        } else {
            val stateSystemInterval = ss.querySingleState(ts, attributeQuark)
            if (stateSystemInterval.startTime <= ts && stateSystemInterval.endTime >= ts2) {
                val interval = createInterval(ss, treeElem, stateSystemInterval)
                modelIntervals.add(interval)
            }
        }

        return modelIntervals
    }


    private fun roundToClosestHigherMultiple(number: Long, multipleOf: Long): Long {
        return (Math.ceil(number.toDouble() / multipleOf) * multipleOf).toLong()
    }

    private fun fillWithMultiStates(timeRange: TimeRange,
                                    treeElem: TimeGraphTreeElement,
                                    modelIntervals: List<TimeGraphStateInterval>): List<TimeGraphStateInterval> {

        if (modelIntervals.size < 2) {
            return modelIntervals
        }

        val filledIntervals = LinkedList<TimeGraphStateInterval>()

        /*
         * Add the first real interval. There might be a multi-state at the
         * beginning.
         */
        val firstRealIntervalStartTime = modelIntervals[0].startTime
        if (modelIntervals[0].startTime > timeRange.startTime) {
            filledIntervals.add(MultiStateInterval(timeRange.startTime, firstRealIntervalStartTime - 1, treeElem))
        }
        filledIntervals.add(modelIntervals[0])

        for (i in 1 until modelIntervals.size) {
            val interval1 = modelIntervals[i - 1]
            val interval2 = modelIntervals[i]
            val bound1 = interval1.endTime
            val bound2 = interval2.startTime

            /* (we've already inserted 'interval1' on the previous loop.) */
            if (bound1 + 1 != bound2) {
                val multiStateInterval = MultiStateInterval(bound1 + 1, bound2 - 1, treeElem)
                filledIntervals.add(multiStateInterval)
            }
            filledIntervals.add(interval2)
        }

        /* Add a multi-state at the end too, if needed */
        val lastRealIntervalEndTime = modelIntervals.last().endTime
        if (lastRealIntervalEndTime < timeRange.endTime) {
            filledIntervals.add(MultiStateInterval(lastRealIntervalEndTime + 1, timeRange.endTime, treeElem))
        }

        return filledIntervals
    }

}
