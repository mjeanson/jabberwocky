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
import com.efficios.jabberwocky.trace.event.TraceEvent
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

abstract class TraceProjectIteratorSingleTraceTestBase {

    protected abstract val trace: Trace<TraceEvent>

    protected abstract val event1: TraceEvent
    protected abstract val event2: TraceEvent
    protected abstract val event3: TraceEvent
    protected abstract val timestampBetween1and2: Long

    protected abstract val middleEvent: TraceEvent
    protected abstract val middleEventPosition: Int

    protected abstract val lastEvent: TraceEvent
    protected abstract val timestampAfterEnd: Long

    private lateinit var projectPath: Path

    protected lateinit var iterator: TraceProjectIterator<TraceEvent>
        private set

    @Before
    fun setup() {
        val projectName = "project-iterator-test"
        projectPath = Files.createTempDirectory(projectName)

        val collection = TraceCollection(listOf(trace))
        val project = TraceProject(projectName, projectPath, listOf(collection))
        iterator = project.iterator()
    }

    @After
    fun cleanup() {
        iterator.close()
        projectPath.toFile().deleteRecursively()
    }

    @Test
    fun testInitial() {
        with(iterator) {
            Assert.assertTrue(hasNext())
            Assert.assertEquals(event1, next())
            Assert.assertEquals(event2, next())
            Assert.assertEquals(event3, next())
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
            Assert.assertEquals(event1, next())
        }
    }

    @Test
    fun testSeekAtBegin() {
        with(iterator) {
            repeat(2, { next() })
            seek(event1.timestamp)
            Assert.assertEquals(event1, next())
        }
    }

    @Test
    fun testSeekBetweenEvents() {
        with(iterator) {
            seek(timestampBetween1and2)
            Assert.assertEquals(event2, next())
        }
    }

    @Test
    fun testSeekAtEvent() {
        with(iterator) {
            seek(event2.timestamp)
            Assert.assertEquals(event2, next())
        }
    }

    @Test
    fun testSeekAtEnd() {
        with(iterator) {
            seek(event3.timestamp)
            Assert.assertEquals(event3, next())
        }
    }

    @Test
    fun testSeekAfterEnd() {
        with(iterator) {
            seek(timestampAfterEnd)
            Assert.assertFalse(hasNext())
        }
    }

    // ------------------------------------------------------------------------
    // previous()/hasPrevious() tests
    // ------------------------------------------------------------------------

    @Test
    fun testPreviousInitial() {
        Assert.assertFalse(iterator.hasPrevious())
    }

    @Test
    fun testPreviousAfterEnd() {
        with(iterator) {
            seek(timestampAfterEnd)
            Assert.assertFalse(hasNext())
            Assert.assertTrue(hasPrevious())
            Assert.assertEquals(lastEvent, previous())
        }
    }

    @Test
    fun testPreviousToBeginning() {
        with(iterator) {
            seek(event3.timestamp)
            Assert.assertTrue(hasPrevious())
            previous()
            Assert.assertTrue(hasPrevious())
            val event = previous()
            Assert.assertEquals(event1, event)
            Assert.assertFalse(hasPrevious())

        }
    }

    @Test
    fun testBackAndForth() {
        with(iterator) {
            seek(middleEvent.timestamp)
            repeat(2, { previous() })
            repeat(2, { next() })
            Assert.assertEquals(middleEvent, next())

            repeat(2, { next() })
            repeat(2, { previous() })
            Assert.assertEquals(middleEvent, previous())
        }
    }

    @Test
    fun testPreviousToBeginningFromMiddle() {
        with(iterator) {
            seek(middleEvent.timestamp)
            var lastReadEvent: TraceEvent? = null
            try {
                repeat(middleEventPosition, {
                    lastReadEvent = previous()
                })
            } catch (e: NoSuchElementException) {
                System.err.println("Last read event: $lastReadEvent")
                throw e
            }
            Assert.assertFalse("Last read event: $lastReadEvent", hasPrevious())
            Assert.assertEquals(event1, lastReadEvent)
        }
    }

    // ------------------------------------------------------------------------
    // copy() tests
    // TODO project iterator don't support copy() yet
    // ------------------------------------------------------------------------

//    @Test
//    fun testCopyStart() {
//        using {
//            with(iterator.copy().autoClose()) {
//                Assert.assertTrue(hasNext())
//                Assert.assertEquals(event1, next())
//                Assert.assertEquals(event2, next())
//                Assert.assertEquals(event3, next())
//            }
//        }
//    }
//
//    @Test
//    fun testCopyMiddleNext() {
//        iterator.next()
//        using { with(iterator.copy().autoClose()) {
//            Assert.assertTrue(hasNext())
//            Assert.assertEquals(event2, next())
//            Assert.assertEquals(event3, next())
//        }}
//    }
//
//    @Test
//    fun testCopyMiddleSeek() {
//        iterator.seek(event2.timestamp)
//        using { with(iterator.copy().autoClose()) {
//            Assert.assertTrue(hasNext())
//            Assert.assertEquals(event2, next())
//            Assert.assertEquals(event3, next())
//        }}
//    }
//
//    @Test
//    fun testCopyEnd() {
//        iterator.seek(timestampAfterEnd)
//        using { with(iterator.copy().autoClose()) {
//            Assert.assertFalse(hasNext())
//        }}
//    }
//
//    @Test
//    fun testCopyOfCopy() {
//        using {
//            val copy1 = iterator.copy().autoClose()
//            val copy2 = copy1.copy().autoClose()
//
//            listOf(copy1, copy2).forEach {
//                // deal
//                with(it) {
//                    Assert.assertEquals(event1, next())
//                    Assert.assertEquals(event2, next())
//                    Assert.assertEquals(event3, next())
//                }
//            }
//        }
//    }
//
//    @Test
//    fun testUsedCopyOfCopy() {
//        iterator.seek(event2.timestamp)
//        using {
//            val copy1 = iterator.copy().autoClose()
//            val copy2 = copy1.copy().autoClose()
//
//            listOf(copy1, copy2).forEach {
//                with(it) {
//                    Assert.assertEquals(event2, next())
//                    Assert.assertEquals(event3, next())
//                }
//            }
//        }
//    }
//
//    @Test
//    fun testSeekAndCopy() {
//        using {
//            val iter1 = iterator
//            iter1.seek(event3.timestamp)
//            val iter2 = iter1.copy().autoClose()
//            iter1.seek(lastEvent.timestamp)
//            iter2.seek(event2.timestamp)
//
//            Assert.assertEquals(lastEvent, iter1.next())
//            Assert.assertEquals(event2, iter2.next())
//        }
//    }

}
