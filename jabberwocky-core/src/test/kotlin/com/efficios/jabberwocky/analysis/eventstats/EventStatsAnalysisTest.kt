/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.analysis.eventstats

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval
import com.efficios.jabberwocky.collection.TraceCollection
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.trace.TraceStubs
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals

class EventStatsAnalysisTest {

    companion object {
        private const val PROJECT_NAME = "Test-eventstats-project"
    }

    private lateinit var projectPath: Path
    private lateinit var ss : ITmfStateSystem

    @Before
    fun setup() {
        /* Setup the trace project */
        projectPath = Files.createTempDirectory(PROJECT_NAME)
        val collection = TraceCollection(listOf(TraceStubs.TraceStub1(), TraceStubs.TraceStub2()))
        val project = TraceProject(PROJECT_NAME, projectPath, listOf(collection))

        /* Setup and execute the analysis */
        val analysis = EventStatsAnalysis()
        ss = analysis.execute(project)
    }

    @After
    fun cleanup() {
        ss.dispose()
        projectPath.toFile().deleteRecursively()
    }

    @Test
    fun testResults() {
        val totalsQuark = ss.getQuarkAbsolute(EventStatsAnalysis.TOTAL_ATTRIBUTE)
        val cpusQuark = ss.getQuarkAbsolute(EventStatsAnalysis.CPU_ATTRIBUTE)
        val eventNamesQuark = ss.getQuarkAbsolute(EventStatsAnalysis.EVENT_NAME_ATTRIBUTE)

        assertEquals(8, ss.nbAttributes)

        /* Test values in the middle */
        val results = ss.queryFullState(6).map{ it.stateValue }
        with(results) {
            assertEquals(4, get(totalsQuark).unboxInt())

            assertEquals(3, get(ss.getQuarkRelative(cpusQuark, "0")).unboxInt())
            assertEquals(1, get(ss.getQuarkRelative(cpusQuark, "1")).unboxInt())

            assertEquals(1, get(ss.getQuarkRelative(eventNamesQuark, TraceStubs.EVENT_NAME_A)).unboxInt())
            assertEquals(3, get(ss.getQuarkRelative(eventNamesQuark, TraceStubs.EVENT_NAME_B)).unboxInt())
            /* The value is null if we have never seen the event before */
            assertTrue(get(ss.getQuarkRelative(eventNamesQuark, TraceStubs.EVENT_NAME_C)).isNull)
        }

        /* Test values at the end */
        val endTime = ss.currentEndTime
        val endResults = ss.queryFullState(endTime).map{ it.stateValue }
        with(endResults) {
            assertEquals(6, get(totalsQuark).unboxInt())

            assertEquals(3, get(ss.getQuarkRelative(cpusQuark, "0")).unboxInt())
            assertEquals(3, get(ss.getQuarkRelative(cpusQuark, "1")).unboxInt())

            assertEquals(2, get(ss.getQuarkRelative(eventNamesQuark, TraceStubs.EVENT_NAME_A)).unboxInt())
            assertEquals(3, get(ss.getQuarkRelative(eventNamesQuark, TraceStubs.EVENT_NAME_B)).unboxInt())
            assertEquals(1, get(ss.getQuarkRelative(eventNamesQuark, TraceStubs.EVENT_NAME_C)).unboxInt())
        }
    }

}