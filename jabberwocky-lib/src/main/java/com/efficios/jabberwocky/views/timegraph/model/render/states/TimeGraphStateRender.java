/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.render.states;

import java.util.List;

import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;
import com.google.common.collect.ImmutableList;

public class TimeGraphStateRender {

    private final long fStartTime;
    private final long fEndTime;
    private final TimeGraphTreeElement fTreeElement;
    private final List<TimeGraphStateInterval> fStateIntervals;

    public TimeGraphStateRender(long startTime,
            long endTime,
            TimeGraphTreeElement treeElement,
            List<TimeGraphStateInterval> stateIntervals) {

        fStartTime = startTime;
        fEndTime = endTime;
        fTreeElement = treeElement;
        fStateIntervals = ImmutableList.copyOf(stateIntervals);
    }

    public long getStartTime() {
        return fStartTime;
    }

    public long getEndTime() {
        return fEndTime;
    }

    public TimeGraphTreeElement getTreeElement() {
        return fTreeElement;
    }

    public List<TimeGraphStateInterval> getStateIntervals() {
        return fStateIntervals;
    }

}
