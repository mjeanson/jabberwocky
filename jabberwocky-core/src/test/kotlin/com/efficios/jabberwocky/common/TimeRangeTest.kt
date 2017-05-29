/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.common

import org.junit.Assert.*
import org.junit.Test

/**
 * Test suite for the [TimeRange] class.
 */
class TimeRangeTest {

    companion object {
        private val FIXTURE = TimeRange.of(20, 30)
    }

    /**
     * Test that attempting to build a time range with invalid values is forbidden.
     */
    @Test(expected = IllegalArgumentException::class)
    fun testBadValues() {
        TimeRange.of(20, 10)
    }

    /**
     * Test the [TimeRange.startTime] property.
     */
    @Test
    fun testStartTime() {
        val start = FIXTURE.startTime
        assertEquals(20, start)
    }

    /**
     * Test the [TimeRange.endTime] property.
     */
    @Test
    fun testEndTime() {
        val end = FIXTURE.endTime
        assertEquals(30, end)
    }

    /**
     * Test the [TimeRange.duration] property.
     */
    @Test
    fun testDuration() {
        val duration = FIXTURE.duration
        assertEquals(10, duration)
    }

    /**
     * Test the [TimeRange.isSingleTimestamp] property.
     */
    @Test
    fun testIsSingleTimestamp() {
        assertFalse(FIXTURE.isSingleTimestamp)

        val singleRange = TimeRange.of(10, 10)
        assertTrue(singleRange.isSingleTimestamp)
    }

    /**
     * Test the [TimeRange.contains] method.
     */
    @Test
    fun testContains() {
        assertTrue(FIXTURE.contains(23))
        assertFalse(FIXTURE.contains(10))
        assertFalse(FIXTURE.contains(50))

        /* contains() is inclusive */
        assertTrue(FIXTURE.contains(20))
        assertTrue(FIXTURE.contains(30))
        assertFalse(FIXTURE.contains(19))
        assertFalse(FIXTURE.contains(31))
    }

}
