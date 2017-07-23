/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.provider.arrows;

import java.util.concurrent.FutureTask;

import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.arrows.TimeGraphArrowSeries;
import org.eclipse.jdt.annotation.Nullable;
import com.efficios.jabberwocky.views.timegraph.model.render.arrows.TimeGraphArrowRender;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.project.ITraceProject;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;

/**
 * Provider for timegraph arrow series.
 *
 * It can be used stand-alone (ie, for testing) but usually would be part of a
 * {@link ITimeGraphModelProvider}.
 *
 * @author Alexandre Montplaisir
 */
public interface ITimeGraphModelArrowProvider {

    /**
     * Property representing the trace this arrow provider fetches its data for.
     *
     * @return The trace property
     */
    ObjectProperty<@Nullable ITraceProject<?, ?>> traceProjectProperty();

    /**
     * Property indicating if this specific arrow provider is currently enabled
     * or not.
     *
     * Normally the controls should not send queries to this provider if it is
     * disabled (they would only query this state), but the arrow provider is
     * free to make use of this property for other reasons.
     *
     * @return The enabled property
     */
    BooleanProperty enabledProperty();

    /**
     * Get the arrow series supplied by this arrow provider. A single provider
     * supplies one and only one arrow series.
     *
     * @return The arrow series
     */
    TimeGraphArrowSeries getArrowSeries();

    /**
     * Get a render of arrows from this arrow provider.
     *
     * @param treeRender
     *            The tree render for which the query is done
     * @param timeRange
     *            The time range of the query. The provider may decide to
     *            include arrows partially inside this range, or not.
     * @param task
     *            An optional task parameter, which can be checked for
     *            cancellation to stop processing at any point and return.
     * @return The corresponding arrow render
     */
    TimeGraphArrowRender getArrowRender(TimeGraphTreeRender treeRender, TimeRange timeRange, @Nullable FutureTask<?> task);

}
