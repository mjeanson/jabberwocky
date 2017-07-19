/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.timegraph.model.provider.arrows;

import org.eclipse.jdt.annotation.Nullable;
import com.efficios.jabberwocky.timegraph.model.render.arrows.TimeGraphArrowSeries;

import com.efficios.jabberwocky.project.ITraceProject;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Basic implementation of {@link ITimeGraphModelArrowProvider}. It takes care
 * of basic definitions of the interface, and lets query definition (the
 * {@link #getArrowRender} method) up to the subclass.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TimeGraphModelArrowProvider implements ITimeGraphModelArrowProvider {

    private final ObjectProperty<@Nullable ITraceProject<?, ?>> fTraceProperty = new SimpleObjectProperty<>(null);
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
    public final ObjectProperty<@Nullable ITraceProject<?, ?>> traceProjectProperty() {
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
