/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.context;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.project.TraceProject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty;

/**
 * A common context for a group of views. Information is stored as properties,
 * and views can add listeners to get notified of value changes.
 *
 * @author Alexandre Montplaisir
 */
class ViewGroupContext {

    companion object {
        /** Value representing uninitialized timestamps */
        @JvmField
        val UNINITIALIZED_RANGE = TimeRange.of(0, 0);

        private val DEFAULT_INITIAL_OFFSET: Long = 100_000_000 // 100 ms (0.1 s)
    }

    /** The current active trace project */
    private val currentTraceProjectProperty: ObjectProperty<TraceProject<*, *>?> = SimpleObjectProperty()
    /** Make sure to use traceProject.set() to set new projects */
    fun currentTraceProjectProperty(): ReadOnlyObjectProperty<TraceProject<*, *>?> = currentTraceProjectProperty
    var currentTraceProject
        get() = currentTraceProjectProperty.get()
        set(value) = setNewTraceProject(value)

    /** Current visible time range in the context */
    private var currentVisibleTimeRangeProperty: ObjectProperty<TimeRange> = SimpleObjectProperty(UNINITIALIZED_RANGE)
    fun currentVisibleTimeRangeProperty() = currentVisibleTimeRangeProperty
    var currentVisibleTimeRange: TimeRange
        get() = currentVisibleTimeRangeProperty.get()
        set(value) = currentVisibleTimeRangeProperty.set(value)

    /** Current time range selection */
    private var currentSelectionTimeRangeProperty: ObjectProperty<TimeRange> = SimpleObjectProperty(UNINITIALIZED_RANGE)
    fun currentSelectionTimeRangeProperty() = currentSelectionTimeRangeProperty
    var currentSelectionTimeRange: TimeRange
        get() = currentSelectionTimeRangeProperty.get()
        set(value) = currentSelectionTimeRangeProperty.set(value)


    private fun setNewTraceProject(traceProject: TraceProject<*, *>?) {
        /* On trace change, adjust the other properties accordingly. */
        if (traceProject == null) {
            currentVisibleTimeRangeProperty = SimpleObjectProperty(UNINITIALIZED_RANGE)
            currentSelectionTimeRangeProperty = SimpleObjectProperty(UNINITIALIZED_RANGE)
        } else {
            val start = traceProject.startTime
            val end = traceProject.endTime
            val visibleRangeEnd = minOf(start + DEFAULT_INITIAL_OFFSET, end);

            currentVisibleTimeRangeProperty = SimpleObjectProperty(TimeRange.of(start, visibleRangeEnd))
            currentSelectionTimeRangeProperty = SimpleObjectProperty(TimeRange.of(start, start))
        }

        currentTraceProjectProperty.set(traceProject);
    }

    /**
     * Utility method to get the full range of the current active trace project.
     *
     * @return The full range of the current trace project, or
     *         {@link #UNINITIALIZED_RANGE} if there is no active project.
     */
    fun getCurrentProjectFullRange(): TimeRange {
        return currentTraceProject?.fullRange ?: UNINITIALIZED_RANGE
    }
}