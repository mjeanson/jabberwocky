/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.views.json;

import java.util.List;

import com.efficios.jabberwocky.views.timegraph.control.TimeGraphModelControl;
import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelRenderProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateRender;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import com.efficios.jabberwocky.views.timegraph.view.TimeGraphModelView;

public class TimeGraphJsonOutput extends TimeGraphModelView {

    public TimeGraphJsonOutput(TimeGraphModelControl control) {
        super(control);
    }

    @Override
    public void disposeImpl() {
    }

    @Override
    public void clear() {
        // TODO
    }

    @Override
    public void seekVisibleRange(long visibleWindowStartTime, long visibleWindowEndTime) {
        /* Generate JSON for the visible area */
        ITimeGraphModelRenderProvider provider = getControl().getModelRenderProvider();

        TimeGraphTreeRender treeRender = provider.getTreeRender(visibleWindowStartTime, visibleWindowEndTime);
        List<TimeGraphStateRender> stateRenders = provider.getStateRenders(treeRender, 1);

        RenderToJson.printRenderTo(stateRenders);
    }

    @Override
    public void drawSelection(long selectionStartTime, long selectionEndTime) {
        // TODO NYI
    }

}
