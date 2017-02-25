/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.views.threads;

import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelRenderProvider.FilterMode;
import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelRenderProvider.SortingMode;

public interface ControlFlowConfigModes {

    SortingMode SORTING_BY_TID = new SortingMode(Messages.ControlFlowSortingModes_ByTid);

    SortingMode SORTING_BY_THREAD_NAME = new SortingMode(Messages.ControlFlowSortingModes_ByThreadName);

    FilterMode FILTERING_INACTIVE_ENTRIES = new FilterMode(Messages.ControlFlowFilterModes_InactiveEntries);
}
