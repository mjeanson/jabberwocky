/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace

import com.efficios.jabberwocky.trace.event.TraceEvent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse

class TraceIteratorTest {

    private val trace = TraceStubs.TraceStub1()

    private lateinit var iterator: TraceIterator<TraceEvent>

    @Before
    fun setup() {
        iterator = trace.iterator()
    }

    @After
    fun cleanup() {
        iterator.close()
    }

    @Test
    fun testInitial() {
        with(iterator) {
            assertTrue(hasNext())
            assertEquals(trace.events[0], next())
            assertEquals(trace.events[1], next())
            assertEquals(trace.events[2], next())
            assertFalse(hasNext())
        }
    }

    @Test
    fun testCopyStart() {
        with(iterator.copy()) {
            assertTrue(hasNext())
            assertEquals(trace.events[0], next())
            assertEquals(trace.events[1], next())
            assertEquals(trace.events[2], next())
            assertFalse(hasNext())
        }
    }

    @Test
    fun testCopyMiddle() {
        iterator.next()
        with(iterator.copy()) {
            assertTrue(hasNext())
            assertEquals(trace.events[1], next())
            assertEquals(trace.events[2], next())
            assertFalse(hasNext())
        }
    }

    @Test
    fun testCopyEnd() {
        repeat(3, { iterator.next() })
        with(iterator.copy()) {
            assertFalse(hasNext())
        }
    }
}