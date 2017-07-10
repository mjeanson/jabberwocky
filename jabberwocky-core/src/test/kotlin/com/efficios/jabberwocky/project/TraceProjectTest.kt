/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.project

import com.efficios.jabberwocky.collection.TraceCollection
import com.efficios.jabberwocky.trace.Trace
import com.efficios.jabberwocky.trace.TraceStubs
import com.efficios.jabberwocky.trace.event.TraceEvent
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals

class TraceProjectTest {

    private lateinit var projectPath: Path
    private lateinit var project: ITraceProject<TraceEvent, Trace<TraceEvent>>

    @Before
    fun setup() {
        val projectName = "test-project"
        projectPath = Files.createTempDirectory(projectName)

        val collection = TraceCollection(listOf(TraceStubs.TraceStub1(), TraceStubs.TraceStub2()))
        project = TraceProject(projectName, projectPath, listOf(collection))
    }

    @After
    fun cleanup() {
        projectPath.toFile().deleteRecursively()
    }

    @Test
    fun testStartTime() {
        assertEquals(2L, project.startTime)
    }

    @Test
    fun testEndTime() {
        assertEquals(10L, project.endTime)
    }
}