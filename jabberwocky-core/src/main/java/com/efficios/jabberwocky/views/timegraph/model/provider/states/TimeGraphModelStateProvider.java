/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.provider.states;

import com.efficios.jabberwocky.project.TraceProject;
import com.efficios.jabberwocky.views.timegraph.model.render.StateDefinition;
import com.google.common.collect.ImmutableList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

/**
 * Basic implementation of {@link ITimeGraphModelStateProvider}.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TimeGraphModelStateProvider implements ITimeGraphModelStateProvider {

    private final ObjectProperty<@Nullable TraceProject<?, ?>> fTraceProjectProperty = new SimpleObjectProperty<>(null);

    private final List<StateDefinition> fStateDefinitions;

    /**
     * Constructor
     *
     * @param stateDefinitions
     *            The state definitions used in this provider
     */
    public TimeGraphModelStateProvider(List<StateDefinition> stateDefinitions) {
        fStateDefinitions = ImmutableList.copyOf(stateDefinitions);
    }

    @Override
    public final ObjectProperty<@Nullable TraceProject<?, ?>> traceProjectProperty() {
        return fTraceProjectProperty;
    }

    @Override
    public final List<StateDefinition> getStateDefinitions() {
        return fStateDefinitions;
    }

}
