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
import kotlin.test.assertFalse

class TraceProjectTest {

    private lateinit var projectPath: Path
    private lateinit var project: TraceProject<TraceEvent, Trace<TraceEvent>>

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

    @Test
    fun testSingleTraceFactoryMethod() {
        val projectName1 = "test-proj1"
        val projectName2 = "test-proj2"
        val projectPath1 = Files.createTempDirectory(projectName1)
        val projectPath2 = Files.createTempDirectory(projectName2)
        try {

            val collection1 = TraceCollection(listOf(TraceStubs.TraceStub1()))
            val project1 = TraceProject(projectName1, projectPath1, listOf(collection1))
            val project2 = TraceProject.ofSingleTrace(projectName2, projectPath2, TraceStubs.TraceStub1())

            /* Ensure both projects see the same events */
            // Java's try-with-resources would look better here!
            val iter1 = project1.iterator()
            val iter2 = project2.iterator()
            try {
                while(iter1.hasNext()) {
                    val event1 = iter1.next()
                    val event2 = iter2.next()
                    assertEquals(event1, event2)
                }
                assertFalse(iter2.hasNext())

            } finally {
                iter1.close()
                iter2.close()
            }


        } finally {
            projectPath1.toFile().deleteRecursively()
            projectPath2.toFile().deleteRecursively()
        }
    }
}