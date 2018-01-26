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

    companion object {
        /** Time ranges that should intersect 'fixture' */
        private val INTERSECTING_RANGES = listOf(
                15 to 20,
                15 to 25,
                20 to 25,
                22 to 28,
                25 to 30,
                25 to 35,
                30 to 35,

                15 to 35,
                15 to 30,
                20 to 30,
                20 to 35,

                20 to 20,
                25 to 25,
                30 to 30)
                .map { TimeRange.of(it.first.toLong(), it.second.toLong()) }

        /** Time ranges that should not intersect 'fixture' */
        private val NON_INTERSECTING_RANGES = listOf(
                12 to 18,
                32 to 38,
                15 to 15,
                35 to 35)
                .map { TimeRange.of(it.first.toLong(), it.second.toLong()) }
    }

    @Test
    fun testIntersects() {
        INTERSECTING_RANGES.forEach { assertTrue(it.intersects(fixture)) }
        NON_INTERSECTING_RANGES.forEach { assertFalse(it.intersects(fixture)) }
    }

    @Test
    fun testIntersection() {
        val intersections = listOf(
                20 to 20,
                20 to 25,
                20 to 25,
                22 to 28,
                25 to 30,
                25 to 30,
                30 to 30,

                20 to 30,
                20 to 30,
                20 to 30,
                20 to 30,

                20 to 20,
                25 to 25,
                30 to 30)
                .map { TimeRange.of(it.first.toLong(), it.second.toLong()) }

        INTERSECTING_RANGES.forEachIndexed { index, timeRange -> assertEquals(intersections[index], timeRange.intersection(fixture)) }
        
        NON_INTERSECTING_RANGES.forEach { assertNull(it.intersection(fixture)) }
    }
}
