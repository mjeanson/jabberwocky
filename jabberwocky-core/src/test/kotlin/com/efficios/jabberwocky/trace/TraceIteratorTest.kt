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
import com.efficios.jabberwocky.utils.using
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

    // ------------------------------------------------------------------------
    // seek() tests
    // ------------------------------------------------------------------------

    @Test
    fun testSeekBeforeBegin() {
        with(iterator) {
            /* Read some events, then seek back to the beginning */
            repeat(2, { next() })
            seek(0)
            assertEquals(trace.events[0], next())
        }
    }

    @Test
    fun testSeekAtBegin() {
        with(iterator) {
            repeat(2, { next() })
            seek(trace.events[0].timestamp)
            assertEquals(trace.events[0], next())
        }
    }

    @Test
    fun testSeekBetweenEvents() {
        with(iterator) {
            seek(3)
            assertEquals(trace.events[1], next())
        }
    }

    @Test
    fun testSeekAtEvent() {
        with(iterator) {
            seek(trace.events[1].timestamp)
            assertEquals(trace.events[1], next())
        }
    }

    @Test
    fun testSeekAtEnd() {
        with(iterator) {
            seek(trace.events[2].timestamp)
            assertEquals(trace.events[2], next())
        }
    }

    @Test
    fun testSeekAfterEnd() {
        with(iterator) {
            seek(12)
            assertFalse(hasNext())
        }
    }

    // ------------------------------------------------------------------------
    // copy() tests
    // ------------------------------------------------------------------------

    @Test
    fun testCopyStart() {
        using {
            with(iterator.copy().autoClose()) {
                assertTrue(hasNext())
                assertEquals(trace.events[0], next())
                assertEquals(trace.events[1], next())
                assertEquals(trace.events[2], next())
                assertFalse(hasNext())
            }
        }
    }

    @Test
    fun testCopyMiddleNext() {
        iterator.next()
        using { with(iterator.copy().autoClose()) {
            assertTrue(hasNext())
            assertEquals(trace.events[1], next())
            assertEquals(trace.events[2], next())
            assertFalse(hasNext())
        }}
    }

    @Test
    fun testCopyMiddleSeek() {
        iterator.seek(trace.events[1].timestamp)
        using { with(iterator.copy().autoClose()) {
            assertTrue(hasNext())
            assertEquals(trace.events[1], next())
            assertEquals(trace.events[2], next())
            assertFalse(hasNext())
        }}
    }

    @Test
    fun testCopyEnd() {
        repeat(3, { iterator.next() })
        using { with(iterator.copy().autoClose()) {
            assertFalse(hasNext())
        }}
    }

    @Test
    fun testCopyOfCopy() {
        using {
            val copy1 = iterator.copy().autoClose()
            val copy2 = copy1.copy().autoClose()

            listOf(copy1, copy2).forEach {
                // deal
                with(it) {
                    assertEquals(trace.events[0], next())
                    assertEquals(trace.events[1], next())
                    assertEquals(trace.events[2], next())
                    assertFalse(hasNext())
                }
            }
        }
    }

    @Test
    fun testUsedCopyOfCopy() {
        iterator.seek(trace.events[1].timestamp)
        using {
            val copy1 = iterator.copy().autoClose()
            val copy2 = copy1.copy().autoClose()

            listOf(copy1, copy2).forEach {
                with(it) {
                    assertEquals(trace.events[1], next())
                    assertEquals(trace.events[2], next())
                    assertFalse(hasNext())
                }
            }
        }
    }

    @Test
    fun testSeekAndCopy() {
        with(trace.events) { using {
            val iter1 = iterator
            iter1.seek(get(2).timestamp)
            val iter2 = iter1.copy().autoClose()
            iter1.seek(last().timestamp)
            iter2.seek(get(1).timestamp)

            assertEquals(last(), iter1.next())
            assertEquals(get(1), iter2.next())
        }}
    }
}