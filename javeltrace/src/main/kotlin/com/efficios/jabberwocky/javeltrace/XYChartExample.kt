/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

@file:JvmName("XYChartExample")
package com.efficios.jabberwocky.javeltrace

import com.efficios.jabberwocky.analysis.eventstats.EventStatsXYChartProvider
import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTrace
import com.efficios.jabberwocky.lttng.kernel.trace.LttngKernelTrace
import com.efficios.jabberwocky.lttng.kernel.views.timegraph.threads.ThreadsModelProvider
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.views.timegraph.view.json.RenderToJson
import com.efficios.jabberwocky.views.xychart.view.json.XYChartJsonOutput
import com.google.common.primitives.Longs
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Example of a standalone program generating the JSON for the Threads
 * timegraph model for a given trace and time range.
 */
fun main(args: Array<String>) {

    /* Parse the command-line parameters */
    if (args.size < 2) {
        printUsage()
        return
    }

    val tracePath = args[0]
    val nbPoints = Longs.tryParse(args[1])
    if (nbPoints == null) {
        printUsage()
        return
    }

    /* Create the trace project */
    val projectPath = Files.createTempDirectory("project")
    val trace = GenericCtfTrace(Paths.get(tracePath))
    val project = TraceProject.ofSingleTrace("MyProject", projectPath, trace)

    val range = TimeRange.of(project.startTime, project.endTime)
    val resolution = range.duration / nbPoints

    /* Query for a XY chart render for the whole trace */
    val provider = EventStatsXYChartProvider()
    provider.traceProject = project
    val render = provider.generateRender(provider.series.first(), range, resolution, null)

    XYChartJsonOutput.printRenderTo(System.out, listOf(render))

    /* Cleanup */
    projectPath.toFile().deleteRecursively()
}

private fun printUsage() {
    System.err.println("Cannot parse command-line arguments.")
    System.err.println("Usage: java -jar <jarname>.jar [TRACE PATH] [NB OF DATA POINTS]")
}