/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.collection

import com.efficios.jabberwocky.collection.TraceCollection
import com.efficios.jabberwocky.ctf.trace.ExtractedCtfTestTrace
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTrace
import com.efficios.jabberwocky.project.TraceProject
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

class CtfTraceProjectTest {

    companion object {
        @JvmField @ClassRule
        val ETT1 = ExtractedCtfTestTrace(CtfTestTrace.KERNEL)

        @JvmField @ClassRule
        val ETT2 = ExtractedCtfTestTrace(CtfTestTrace.TRACE2)

        @JvmField @ClassRule
        val ETT3 = ExtractedCtfTestTrace(CtfTestTrace.KERNEL_VM)

        private val projectName = "Test-project"
    }

    private lateinit var projectPath: Path
    private lateinit var fixture: TraceProject<CtfTraceEvent, GenericCtfTrace>

    @Before
    fun setup() {
        projectPath = Files.createTempDirectory(projectName)

        /* Put the first two traces in one collection, and the third one by itself */
        val collection1 = TraceCollection(listOf(ETT1.trace, ETT2.trace))
        val collection2 = TraceCollection(listOf(ETT3.trace))
        fixture = TraceProject(projectName, projectPath, listOf(collection1, collection2))
    }

    @After
    fun cleanup() {
        projectPath.toFile().deleteRecursively()
    }

    @Test
    fun testEventCount() {
        val expectedCount = CtfTestTrace.KERNEL.nbEvents + CtfTestTrace.TRACE2.nbEvents + CtfTestTrace.KERNEL_VM.nbEvents
        var actualCount: Int = 0
        fixture.iterator().use {
            actualCount = it.asSequence().count()
        }
        assertEquals(expectedCount, actualCount)
    }

    // TODO More tests, especially with overlapping traces
}
