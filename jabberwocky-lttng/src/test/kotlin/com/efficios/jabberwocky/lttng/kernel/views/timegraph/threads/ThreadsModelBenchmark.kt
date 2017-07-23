/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

@file:JvmName("ThreadsModelBenchmark")

package com.efficios.jabberwocky.lttng.kernel.views.timegraph.threads

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.lttng.kernel.trace.LttngKernelTrace
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.views.timegraph.model.provider.states.ITimeGraphModelStateProvider
import com.efficios.jabberwocky.views.timegraph.model.render.states.TimeGraphStateRender
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender
import java.nio.file.Files
import java.nio.file.Paths

private const val PROJECT_NAME = "benchmark-project"
private const val RUNS = 10
private const val TARGET_NB_PIXELS = 2000

/**
 * Benchmark of the {@link ThreadsModelProvider} and
 * {@link ThreadsModelStateProvider} for a target trace
 * passed as parameter.
 */
fun main(args: Array<String>) {

    if (args.isEmpty()) {
        printErr("Please include the path to a LTTng kernel trace as parameter.")
        return
    }

    /* Setup the trace and project */
    val tracePath = args[0]
    val projectPath = Files.createTempDirectory(PROJECT_NAME)
    val trace = LttngKernelTrace(Paths.get(tracePath))
    println("Creating project from trace $tracePath")
    val traceProject = TraceProject.ofSingleTrace(PROJECT_NAME, projectPath, trace)

    /* Compute the time ranges that will be used for queries */
    val traceStart = traceProject.startTime
    val traceEnd = traceProject.endTime
    val time_1_8 = ((traceEnd - traceStart) / 8) + traceStart
    val time_2_8 = (2 * (traceEnd - traceStart) / 8) + traceStart
    val time_6_8 = (6 * (traceEnd - traceStart) / 8) + traceStart
    val time_7_8 = (7 * (traceEnd - traceStart) / 8) + traceStart
    val tr1 = TimeRange.of(time_1_8, time_2_8)
    val tr2 = TimeRange.of(time_6_8, time_7_8)
    val resolution = tr1.duration / TARGET_NB_PIXELS
    println("resolution=$resolution")

    /* Setup the model provider */
    val modelProvider = ThreadsModelProvider()
    modelProvider.traceProject = traceProject

    val treeRender = modelProvider.treeRender
    if (treeRender == TimeGraphTreeRender.EMPTY_RENDER) {
        printErr("Analysis produced an empty tree model. Exiting.")
        return
    }
    val stateProvider = modelProvider.stateProvider

    /* Do a first set of queries to prime the caches. */
    println("Priming queries...")
    query(stateProvider, treeRender, tr1, resolution)
    query(stateProvider, treeRender, tr2, resolution)

    println("Querying")
    val results = (1..RUNS)
            .onEach { println("Run #$it of $RUNS") }
            .map {
                val start = System.nanoTime()
                query(stateProvider, treeRender, tr1, resolution)
                val end = System.nanoTime()

                /* Second query elsewhere to wipe the cache locality */
                query(stateProvider, treeRender, tr2, resolution)
                end - start
            }

    println()
    val avg = results.average()
    println("Benchmarked state model for trace $tracePath for range $tr1, averaged over $RUNS runs.")
    println("$avg ns")


    /* Cleanup */
    projectPath.toFile().deleteRecursively()
}

/** Do a query for the given time range */
private fun query(
        stateProvider: ITimeGraphModelStateProvider,
        treeRender: TimeGraphTreeRender,
        timeRange: TimeRange,
        resolution: Long): List<TimeGraphStateRender> =
    treeRender.allTreeElements
            .onEach { println("Querying for tree element: $it") }
            .map { stateProvider.getStateRender(it, timeRange, resolution, null) }


private fun printErr(errorMsg: String) {
    System.err.println(errorMsg)
}
