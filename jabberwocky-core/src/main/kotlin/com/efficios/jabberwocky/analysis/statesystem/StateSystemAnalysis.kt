/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.analysis.statesystem

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem
import ca.polymtl.dorsal.libdelorean.ITmfStateSystemBuilder
import ca.polymtl.dorsal.libdelorean.StateSystemFactory
import ca.polymtl.dorsal.libdelorean.backend.StateHistoryBackendFactory
import com.efficios.jabberwocky.analysis.IAnalysis
import com.efficios.jabberwocky.collection.ITraceCollection
import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.project.ITraceProject
import com.efficios.jabberwocky.trace.event.ITraceEvent
import java.io.IOException
import java.nio.file.Files

abstract class StateSystemAnalysis(override val name: String) : IAnalysis  {

    companion object {
        private const val ANALYSES_DIRECTORY = "analyses"
        private const val HISTORY_FILE_EXTENSION = ".ht"
    }

    final override fun execute(project: ITraceProject<*, *>, range: TimeRange?, extraParams: String?): ITmfStateSystem {
        if (range != null) throw UnsupportedOperationException("Partial ranges for state system analysis not yet implemented")
//        if (extraParams != null) logWarning("Ignoring extra parameters: $extraParams")

        /* Determine the path of the history tree backing file we expect */
        val analysisClassName = javaClass.toString()
        val analysesDirectory = project.directory.resolve(ANALYSES_DIRECTORY)
        if (!Files.exists(analysesDirectory)) Files.createDirectory(analysesDirectory)

        val stateSystemFile = analysesDirectory.resolve(analysisClassName + HISTORY_FILE_EXTENSION)
        var newFile = !Files.exists(stateSystemFile)

        /* Create the history tree backend we will use */
        val htBackend = if (Files.exists(stateSystemFile)) {
            try {
                StateHistoryBackendFactory.createHistoryTreeBackendExistingFile(analysisClassName, stateSystemFile.toFile(), providerVersion)
            } catch (e: IOException) {
                /* The expected provider version may not match what we have on disk. Try building the file from scratch instead */
                newFile = true
                StateHistoryBackendFactory.createHistoryTreeBackendNewFile(analysisClassName, stateSystemFile.toFile(), providerVersion, project.startTime)
            }
        } else {
            StateHistoryBackendFactory.createHistoryTreeBackendNewFile(analysisClassName, stateSystemFile.toFile(), providerVersion, project.startTime)
        }

        val ss = StateSystemFactory.newStateSystem(htBackend, newFile)
        /* If there was a history file already built, it should be good to go. */
        if (newFile) {
            buildForProject(project, ss)
        }
        return ss
    }

    private fun buildForProject(project: ITraceProject<*, *>, stateSystem: ITmfStateSystemBuilder) {
        val traces = filterTraces(project)
        // TODO This iteration could eventually moved to a central location, so that the events are
        // read once then dispatched to several "state providers".
        // However some analyses may not need all events from all traces in a project. We'll see...
        var latestTimestamp = project.startTime
        traces.iterator().use {
            while(it.hasNext()) {
                val event = it.next()
                handleEvent(stateSystem, event)
                latestTimestamp = event.timestamp
            }
        }
        stateSystem.closeHistory(latestTimestamp)
    }

    abstract val providerVersion: Int

    abstract fun filterTraces(project: ITraceProject<*, *>) : ITraceCollection<*, *>

    abstract fun handleEvent(ss: ITmfStateSystemBuilder, event: ITraceEvent)
}