package com.efficios.jabberwocky.timegraph.view.json;

import java.util.List;

import com.efficios.jabberwocky.timegraph.model.render.states.TimeGraphStateRender;
import com.efficios.jabberwocky.timegraph.control.TimeGraphModelControl;
import com.efficios.jabberwocky.timegraph.model.provider.ITimeGraphModelProvider;
import com.efficios.jabberwocky.timegraph.model.render.tree.TimeGraphTreeRender;
import com.efficios.jabberwocky.timegraph.view.TimeGraphModelView;

import com.efficios.jabberwocky.common.TimeRange;

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
    public void seekVisibleRange(TimeRange newVisibleRange) {
        /* Generate JSON for the visible area */
        ITimeGraphModelProvider provider = getControl().getModelRenderProvider();

        TimeGraphTreeRender treeRender = provider.getTreeRender();
        List<TimeGraphStateRender> stateRenders = provider.getStateProvider().getStateRenders(treeRender,
                newVisibleRange, 1, null);

        RenderToJson.printRenderTo(stateRenders);
    }

    @Override
    public void drawSelection(TimeRange selectionRange) {
        // TODO NYI
    }

}
