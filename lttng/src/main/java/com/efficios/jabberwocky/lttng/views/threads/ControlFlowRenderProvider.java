/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.views.threads;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.management.AttributeNotFoundException;

import com.efficios.jabberwocky.views.timegraph.model.provider.statesystem.StateSystemModelRenderProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.ColorDefinition;
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateInterval.LineThickness;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.StateSystemUtils;
import ca.polymtl.dorsal.libdelorean.exceptions.StateValueTypeException;
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.TmfStateValue;

public class ControlFlowRenderProvider extends StateSystemModelRenderProvider {

    private static final List<SortingMode> SORTING_MODES = ImmutableList.of(
            ControlFlowConfigModes.SORTING_BY_TID,
            ControlFlowConfigModes.SORTING_BY_THREAD_NAME);

    private static final List<FilterMode> FILTER_MODES = ImmutableList.of(
            ControlFlowConfigModes.FILTERING_INACTIVE_ENTRIES);

    /**
     * State values that are considered inactive, for purposes of filtering out
     * when the "filter inactive entries" mode is enabled.
     */
    private static final Set<ITmfStateValue> INACTIVE_STATE_VALUES = ImmutableSet.of(
            TmfStateValue.nullValue(),
            StateValues.PROCESS_STATUS_UNKNOWN_VALUE,
            StateValues.PROCESS_STATUS_WAIT_UNKNOWN_VALUE,
            StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE
            );

    /**
     * Each "Thread" attribute has the following children:
     *
     * <ul>
     * <li>Prio</li>
     * <li>System_call</li>
     * <li>Exec_name</li>
     * <li>PPID</li>
     * </ul>
     *
     * The "Thread" is considered the base quark.
     */
    private static final String[] BASE_QUARK_PATTERN = { Attributes.THREADS, "*" }; //$NON-NLS-1$

    /**
     * Get the tree element name for every thread. It consists of the TID
     * followed by the first available exec_name for this thread.
     *
     * FIXME This implies a static tree definition for every TID, which does not
     * handle TID re-use correctly. The state system structure should be updated
     * accordingly.
     */
    @VisibleForTesting
    public static final Function<TreeRenderContext, TimeGraphTreeRender> SS_TO_TREE_RENDER_FUNCTION = (treeContext) -> {
        ITmfStateSystem ss = treeContext.ss;
        List<ITmfStateInterval> fullState = treeContext.fullQueryAtRangeStart;

        Stream<ControlFlowTreeElement> treeElems = ss.getQuarks(BASE_QUARK_PATTERN).stream()
                .map(baseQuark -> {
                    String tid = ss.getAttributeName(baseQuark);

                    String threadName;
                    try {
                        int execNameQuark = ss.getQuarkRelative(baseQuark, Attributes.EXEC_NAME);
                        // TODO We should look starting at treeContext.renderTimeRangeStart
                        // first, and if we don't find anything use ss.getStartTime(), so that
                        // we catch subsequent process name changes
                        ITmfStateInterval firstInterval = StateSystemUtils.queryUntilNonNullValue(ss,
                                execNameQuark, ss.getStartTime(), Long.MAX_VALUE);
                        if (firstInterval == null) {
                            threadName = null;
                        } else {
                            threadName = firstInterval.getStateValue().unboxStr();
                        }
                    } catch (AttributeNotFoundException | StateValueTypeException e) {
                        threadName = null;
                    }

                    return new ControlFlowTreeElement(tid, threadName, Collections.emptyList(), baseQuark);
                });

        /* Run the entries through the active filter modes */
        Set<FilterMode> filterModes = treeContext.filterModes;
        if (filterModes.contains(ControlFlowConfigModes.FILTERING_INACTIVE_ENTRIES)) {
            /*
             * Filter out the tree elements whose state is considered inactive
             * for the whole duration of the configured time range.
             */
            treeElems = treeElems.filter(elem -> {
                ITmfStateInterval interval = fullState.get(elem.getSourceQuark());
                if (interval.getEndTime() > treeContext.renderTimeRangeEnd &&
                        INACTIVE_STATE_VALUES.contains(interval.getStateValue())) {
                    return false;
                }
                return true;
            });
        }

        /* Sort entries according to the active sorting mode */
        SortingMode sortingMode = treeContext.sortingMode;
        if (sortingMode == ControlFlowConfigModes.SORTING_BY_TID) {
            treeElems = treeElems.sorted(Comparator.comparingInt(ControlFlowTreeElement::getTid));
        } else if (sortingMode == ControlFlowConfigModes.SORTING_BY_THREAD_NAME) {
            treeElems = treeElems.sorted((elem1, elem2) -> {
                return elem1.getThreadName().compareToIgnoreCase(elem2.getThreadName());
            });
        }

        List<TimeGraphTreeElement> treeElemsList = treeElems.collect(Collectors.toList());
        return new TimeGraphTreeRender(treeElemsList, treeContext.renderTimeRangeStart, treeContext.renderTimeRangeEnd);
    };



    /**
     * Function mapping state names
     *
     * @param value
     *            State value representing the state
     * @return The state name to display, should be localized
     */
    @VisibleForTesting
    public static String mapStateValueToStateName(int value) {
        try {
            switch (value) {
            case StateValues.PROCESS_STATUS_WAIT_UNKNOWN:
                return Messages.ControlFlowRenderProvider_State_WaitUnknown;
            case StateValues.PROCESS_STATUS_WAIT_BLOCKED:
                return Messages.ControlFlowRenderProvider_State_WaitBlocked;
            case StateValues.PROCESS_STATUS_WAIT_FOR_CPU:
                return Messages.ControlFlowRenderProvider_State_WaitForCpu;
            case StateValues.PROCESS_STATUS_RUN_USERMODE:
                return Messages.ControlFlowRenderProvider_State_UserMode;
            case StateValues.PROCESS_STATUS_RUN_SYSCALL:
                return Messages.ControlFlowRenderProvider_State_Syscall;
            case StateValues.PROCESS_STATUS_INTERRUPTED:
                return Messages.ControlFlowRenderProvider_State_Interrupted;
            default:
                return Messages.ControlFlowRenderProvider_State_Unknown;
            }

        } catch (StateValueTypeException e) {
            return Messages.ControlFlowRenderProvider_State_Unknown;
        }
    }

    private static final Function<StateIntervalContext, String> STATE_NAME_MAPPING_FUNCTION = ssCtx -> {
        int statusQuark = ssCtx.baseTreeElement.getSourceQuark();
        ITmfStateValue val = ssCtx.fullQueryAtIntervalStart.get(statusQuark).getStateValue();
        int status = val.unboxInt();
        return mapStateValueToStateName(status);
    };


    private static final ColorDefinition NO_COLOR           = new ColorDefinition(  0,   0,   0,  0);
    private static final ColorDefinition COLOR_UNKNOWN      = new ColorDefinition(100, 100, 100);
    private static final ColorDefinition COLOR_WAIT_UNKNOWN = new ColorDefinition(200, 200, 200);
    private static final ColorDefinition COLOR_WAIT_BLOCKED = new ColorDefinition(200, 200,   0);
    private static final ColorDefinition COLOR_WAIT_FOR_CPU = new ColorDefinition(200, 100,   0);
    private static final ColorDefinition COLOR_USERMODE     = new ColorDefinition(  0, 200,   0);
    private static final ColorDefinition COLOR_SYSCALL      = new ColorDefinition(  0,   0, 200);
    private static final ColorDefinition COLOR_INTERRUPTED  = new ColorDefinition(200,   0, 100);

    private static final Function<StateIntervalContext, ColorDefinition> COLOR_MAPPING_FUNCTION = ssCtx -> {
        try {
            int statusQuark = ssCtx.baseTreeElement.getSourceQuark();
            ITmfStateValue val = ssCtx.fullQueryAtIntervalStart.get(statusQuark).getStateValue();

            if (val.isNull()) {
                return NO_COLOR;
            }

            int status = val.unboxInt();
            switch (status) {
            case StateValues.PROCESS_STATUS_WAIT_UNKNOWN:
                return COLOR_WAIT_UNKNOWN;
            case StateValues.PROCESS_STATUS_WAIT_BLOCKED:
                return COLOR_WAIT_BLOCKED;
            case StateValues.PROCESS_STATUS_WAIT_FOR_CPU:
                return COLOR_WAIT_FOR_CPU;
            case StateValues.PROCESS_STATUS_RUN_USERMODE:
                return COLOR_USERMODE;
            case StateValues.PROCESS_STATUS_RUN_SYSCALL:
                return COLOR_SYSCALL;
            case StateValues.PROCESS_STATUS_INTERRUPTED:
                return COLOR_INTERRUPTED;
            default:
                return COLOR_UNKNOWN;
            }

        } catch (StateValueTypeException e) {
            return COLOR_UNKNOWN;
        }
    };

    /* No variation for now */
    private static final Function<StateIntervalContext, LineThickness> LINE_THICKNESS_MAPPING_FUNCTION = ssCtx -> {
//        int statusQuark = ssCtx.baseTreeElement.getSourceQuark();
//        ITmfStateValue val = ssCtx.fullQueryAtIntervalStart.get(statusQuark).getStateValue();
//
//        // For demo purposes only!
//        if (val.equals(StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE)) {
//            return LineThickness.SMALL;
//        }

        return LineThickness.NORMAL;
    };

    // TODO
//    private static final Function<StateIntervalContext, @Nullable Supplier<Map<String, String>>> PROPERTY_MAPPING_FUNCTION = ssCtx -> {
//        return null;
//    };

    /**
     * Constructor
     */
    public ControlFlowRenderProvider() {
        super(SORTING_MODES,
                FILTER_MODES,
                /* Parameters specific to state system render providers */
                KernelAnalysisModule.ID,
                SS_TO_TREE_RENDER_FUNCTION,
                STATE_NAME_MAPPING_FUNCTION,
                COLOR_MAPPING_FUNCTION,
                LINE_THICKNESS_MAPPING_FUNCTION);

        enableFilterMode(0);
    }
}
