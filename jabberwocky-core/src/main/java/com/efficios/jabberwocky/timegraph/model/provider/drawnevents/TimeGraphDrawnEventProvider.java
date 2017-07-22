/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.timegraph.model.provider.drawnevents;

import org.eclipse.jdt.annotation.Nullable;
import com.efficios.jabberwocky.timegraph.model.render.drawnevents.TimeGraphDrawnEventSeries;

import com.efficios.jabberwocky.project.ITraceProject;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Basic implementation of {@link ITimeGraphDrawnEventProvider}.
 *
 * Implementation of the {@link #getEventRender} method is left to subclasses.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TimeGraphDrawnEventProvider implements ITimeGraphDrawnEventProvider {

    private final ObjectProperty<@Nullable ITraceProject<?, ?>> fTraceProjectProperty = new SimpleObjectProperty<>(null);
    private final BooleanProperty fEnabledProperty = new SimpleBooleanProperty(false);
    private final TimeGraphDrawnEventSeries fDrawnEventSeries;

    /**
     * Constructor
     *
     * @param drawnEventSeries
     *            The event series provided by this provider.
     */
    protected TimeGraphDrawnEventProvider(TimeGraphDrawnEventSeries drawnEventSeries) {
        fDrawnEventSeries = drawnEventSeries;
    }

    @Override
    public final ObjectProperty<@Nullable ITraceProject<?, ?>> traceProjectProperty() {
        return fTraceProjectProperty;
    }

    @Override
    public final BooleanProperty enabledProperty() {
        return fEnabledProperty;
    }

    @Override
    public final TimeGraphDrawnEventSeries getEventSeries() {
        return fDrawnEventSeries;
    }

}
