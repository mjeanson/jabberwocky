/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.analysis

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem
import ca.polymtl.dorsal.libdelorean.ITmfStateSystemBuilder
import com.efficios.jabberwocky.analysis.AnalysisManager
import com.efficios.jabberwocky.analysis.statesystem.StateSystemAnalysis
import com.efficios.jabberwocky.collection.ITraceCollection
import com.efficios.jabberwocky.collection.TraceCollection
import com.efficios.jabberwocky.ctf.trace.ExtractedCtfTestTrace
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTrace
import com.efficios.jabberwocky.project.ITraceProject
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.trace.event.ITraceEvent
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

class CtfStateSystemAnalysisTest {

    companion object {
        @JvmField @ClassRule
        val ETT1 = ExtractedCtfTestTrace(CtfTestTrace.KERNEL)

        @JvmField @ClassRule
        val ETT2 = ExtractedCtfTestTrace(CtfTestTrace.TRACE2)

        @JvmField @ClassRule
        val ETT3 = ExtractedCtfTestTrace(CtfTestTrace.KERNEL_VM)

        private val projectName = "Test-statesystem-project"
        private val analysisName = "test-ss-analysis"
        private val attribName = "count"
    }

    private lateinit var projectPath: Path
    private lateinit var project: ITraceProject<CtfTraceEvent, GenericCtfTrace>
    private lateinit var analysis: TestAnalysis

    private lateinit var ss : ITmfStateSystem

    @Before
    fun setup() {
        /* Setup the trace project */
        projectPath = Files.createTempDirectory(projectName)
        /* Put the first two traces in one collection, and the third one by itself */
        val collection1 = TraceCollection(listOf(ETT1.trace, ETT2.trace))
        val collection2 = TraceCollection(listOf(ETT3.trace))
        project = TraceProject(projectName, projectPath, listOf(collection1, collection2))

        /* Setup the analysis */
        analysis = TestAnalysis()
        AnalysisManager.registerAnalysis(analysis)

        /* Execute the analysis */
        ss = analysis.execute(project)
    }

    @After
    fun cleanup() {
        ss.dispose()
        projectPath.toFile().deleteRecursively()
        AnalysisManager.unregisterAnalysis(analysis)
    }

    private class TestAnalysis : StateSystemAnalysis(analysisName) {

        override val providerVersion = 0

        override fun appliesTo(project: ITraceProject<*, *>) = true

        override fun canExecute(project: ITraceProject<*, *>) = true

        override fun filterTraces(project: ITraceProject<*, *>): ITraceCollection<*, *> {
            /* Just return all traces in the project */
            return TraceCollection(project.traceCollections.flatMap { it.traces })
        }

        override fun handleEvent(ss: ITmfStateSystemBuilder, event: ITraceEvent) {
            /* Count the number of seen events in a "count" attribute */
            val quark = ss.getQuarkAbsoluteAndAdd(attribName)
            ss.incrementAttribute(event.timestamp, quark)
        }

    }

    @Test
    fun testResults() {
        assertEquals(1, ss.nbAttributes)

        /* Check the event count at the end. */
        val expectedEventCount = CtfTestTrace.KERNEL.nbEvents + CtfTestTrace.TRACE2.nbEvents + CtfTestTrace.KERNEL_VM.nbEvents
        val endTime = ss.currentEndTime
        val quark = ss.getQuarkAbsolute(attribName)
        val eventCount = ss.querySingleState(endTime, quark).stateValue.unboxInt()
        assertEquals(expectedEventCount, eventCount)

        /* Check the event count at some point in the middle. */
        val halfwayTimestamp = 1347684508_932149675L
        val expectedHWCount = 1290960
        val halfwayCount = ss.querySingleState(halfwayTimestamp, quark).stateValue.unboxInt()
        assertEquals(expectedHWCount, halfwayCount)
    }

}