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

    private val fixture = TimeRange.of(20, 30)

    /**
     * Test that attempting to build a time range with invalid values is forbidden.
     */
    @Test(expected = IllegalArgumentException::class)
    fun testBadValues() {
        TimeRange.of(20, 10)
    }

    @Test
    fun testStartTime() = assertEquals(20, fixture.startTime)

    @Test
    fun testEndTime() = assertEquals(30, fixture.endTime)

    @Test
    fun testDuration() = assertEquals(10, fixture.duration)

    @Test
    fun testIsSingleTimestamp() {
        assertFalse(fixture.isSingleTimestamp)
        assertTrue(TimeRange.of(10, 10).isSingleTimestamp)
    }

    @Test
    fun testContains() {
        assertTrue(23 in fixture)
        assertFalse(10 in fixture)
        assertFalse(50 in fixture)

        /* contains() is inclusive */
        assertTrue(20 in fixture)
        assertTrue(30 in fixture)
        assertFalse(19 in fixture)
        assertFalse(31 in fixture)
    }

}
