/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.provider.arrows;

import com.efficios.jabberwocky.project.TraceProject;
import com.efficios.jabberwocky.views.timegraph.model.render.arrows.TimeGraphArrowSeries;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Basic implementation of {@link ITimeGraphModelArrowProvider}. It takes care
 * of basic definitions of the interface, and lets query definition (the
 * {@link #getArrowRender} method) up to the subclass.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TimeGraphModelArrowProvider implements ITimeGraphModelArrowProvider {

    private final ObjectProperty<@Nullable TraceProject<?, ?>> fTraceProperty = new SimpleObjectProperty<>(null);
    private final BooleanProperty fEnabledProperty = new SimpleBooleanProperty(false);
    private final TimeGraphArrowSeries fArrowSeries;

    /**
     * Constructor
     *
     * @param arrowSeries
     *            The arrow series that will be represented by this arrow
     *            provider
     */
    public TimeGraphModelArrowProvider(TimeGraphArrowSeries arrowSeries) {
        fArrowSeries = arrowSeries;
    }

    @Override
    public final ObjectProperty<@Nullable TraceProject<?, ?>> traceProjectProperty() {
        return fTraceProperty;
    }

    @Override
    public BooleanProperty enabledProperty() {
        return fEnabledProperty;
    }

    @Override
    public final TimeGraphArrowSeries getArrowSeries() {
        return fArrowSeries;
    }
}
