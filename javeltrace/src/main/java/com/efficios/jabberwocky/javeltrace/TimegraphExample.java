/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.javeltrace;

import com.efficios.jabberwocky.collection.ITraceCollection;
import com.efficios.jabberwocky.collection.TraceCollection;
import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.lttng.kernel.trace.LttngKernelTrace;
import com.efficios.jabberwocky.lttng.kernel.views.timegraph.threads.ThreadsModelProvider;
import com.efficios.jabberwocky.project.ITraceProject;
import com.efficios.jabberwocky.project.TraceProject;
import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelProvider;
import com.efficios.jabberwocky.views.timegraph.model.provider.states.ITimeGraphModelStateProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateRender;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import com.efficios.jabberwocky.views.timegraph.view.json.RenderToJson;
import com.efficios.jabberwocky.trace.ITrace;
import com.google.common.primitives.Longs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * Example of a standalone program generating the JSON for the Threads
 * timegraph model for a given trace and time range.
 */
public class TimegraphExample {

    public static void main(String[] args) throws Exception {

        /* Parse the command-line parameters */
        String tracePath = args[0];
        long renderStart = Longs.tryParse(args[1]);
        long renderEnd = Longs.tryParse(args[2]);
        long resolution = Longs.tryParse(args[3]);

        Path projectPath = Files.createTempDirectory("project");

        /* Create the trace project */
        ITrace trace = new LttngKernelTrace(Paths.get(tracePath));
        ITraceCollection collection = new TraceCollection(Collections.singleton(trace));
        ITraceProject project = new TraceProject("MyProject", projectPath, Collections.singleton(collection));

        /* Query for a timegraph render for the requested time range */
        ITimeGraphModelProvider modelProvider = new ThreadsModelProvider();
        ITimeGraphModelStateProvider stateModelProvider = modelProvider.getStateProvider();
        modelProvider.setTraceProject(project);

        TimeRange range = TimeRange.of(renderStart, renderEnd);

        TimeGraphTreeRender treeRender = modelProvider.getTreeRender();
        List<TimeGraphStateRender> renders = stateModelProvider.getStateRenders(treeRender, range, resolution, null);
        RenderToJson.printRenderToStdout(renders);
    }

}
