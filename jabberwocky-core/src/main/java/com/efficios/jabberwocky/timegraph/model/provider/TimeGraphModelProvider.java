/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.efficios.jabberwocky.timegraph.model.provider;

import com.efficios.jabberwocky.project.ITraceProject;
import com.efficios.jabberwocky.timegraph.model.provider.arrows.ITimeGraphModelArrowProvider;
import com.efficios.jabberwocky.timegraph.model.provider.states.ITimeGraphModelStateProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.eclipse.jdt.annotation.Nullable;
import com.efficios.jabberwocky.timegraph.model.render.tree.TimeGraphTreeRender;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base implementation of {@link ITimeGraphModelProvider}.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TimeGraphModelProvider implements ITimeGraphModelProvider {

    /**
     * A "default" sorting mode, for use when only one is needed.
     */
    protected static final SortingMode DEFAULT_SORTING_MODE = new SortingMode(Messages.DefaultSortingModeName);

    private final String fName;
    private final List<SortingMode> fSortingModes;
    private final List<FilterMode> fFilterModes;

    private final ITimeGraphModelStateProvider fStateProvider;
    private final List<ITimeGraphModelArrowProvider> fArrowProviders;

    private final Set<FilterMode> fActiveFilterModes = new HashSet<>();
    private SortingMode fCurrentSortingMode;

    private final ObjectProperty<@Nullable ITraceProject<?, ?>> fTraceProjectProperty = new SimpleObjectProperty<>();

    /**
     * Constructor
     *
     * @param name
     *            The name of this provider
     * @param sortingModes
     *            The available sorting modes
     * @param filterModes
     *            The available filter modes
     * @param stateProvider
     *            The state provider part of this model provider
     * @param arrowProviders
     *            The arrow provider(s) supplied by this model provider
     */
    public TimeGraphModelProvider(String name,
            @Nullable List<SortingMode> sortingModes,
            @Nullable List<FilterMode> filterModes,
            ITimeGraphModelStateProvider stateProvider,
            @Nullable List<ITimeGraphModelArrowProvider> arrowProviders) {
        fName = name;

        fStateProvider = stateProvider;
        stateProvider.traceProjectProperty().bind(fTraceProjectProperty);

        if (sortingModes == null || sortingModes.isEmpty()) {
            fSortingModes = ImmutableList.of(DEFAULT_SORTING_MODE);
        } else {
            fSortingModes = ImmutableList.copyOf(sortingModes);

        }
        fCurrentSortingMode = fSortingModes.get(0);

        if (filterModes == null || filterModes.isEmpty()) {
            fFilterModes = ImmutableList.of();
        } else {
            fFilterModes = ImmutableList.copyOf(filterModes);
        }

        if (arrowProviders == null || arrowProviders.isEmpty()) {
            fArrowProviders = ImmutableList.of();
        } else {
            fArrowProviders = ImmutableList.copyOf(arrowProviders);
        }
        fArrowProviders.forEach(ap -> ap.traceProjectProperty().bind(fTraceProjectProperty));
    }

    @Override
    public final String getName() {
        return fName;
    }

    @Override
    public final void setTraceProject(@Nullable ITraceProject<?, ?> trace) {
        fTraceProjectProperty.set(trace);
    }

    @Override
    public final @Nullable ITraceProject<?, ?> getTraceProject() {
        return fTraceProjectProperty.get();
    }

    @Override
    public final ObjectProperty<@Nullable ITraceProject<?, ?>> traceProjectProperty() {
        return fTraceProjectProperty;
    }

    @Override
    public final ITimeGraphModelStateProvider getStateProvider() {
        return fStateProvider;
    }

    @Override
    public final List<ITimeGraphModelArrowProvider> getArrowProviders() {
        return fArrowProviders;
    }

    // ------------------------------------------------------------------------
    // Render generation methods. Implementation left to subclasses.
    // ------------------------------------------------------------------------

    @Override
    public abstract TimeGraphTreeRender getTreeRender();

    // ------------------------------------------------------------------------
    // Sorting modes
    // ------------------------------------------------------------------------

    @Override
    public final List<SortingMode> getSortingModes() {
        return fSortingModes;
    }

    @Override
    public final SortingMode getCurrentSortingMode() {
        return fCurrentSortingMode;
    }

    @Override
    public final void setCurrentSortingMode(int index) {
        fCurrentSortingMode = fSortingModes.get(index);
    }

    // ------------------------------------------------------------------------
    // Filter modes
    // ------------------------------------------------------------------------

    @Override
    public final List<FilterMode> getFilterModes() {
        return fFilterModes;
    }

    @Override
    public final void enableFilterMode(int index) {
        fActiveFilterModes.add(fFilterModes.get(index));
    }

    @Override
    public final void disableFilterMode(int index) {
        fActiveFilterModes.remove(fFilterModes.get(index));
    }

    @Override
    public final Set<FilterMode> getActiveFilterModes() {
        return ImmutableSet.copyOf(fActiveFilterModes);
    }

}
