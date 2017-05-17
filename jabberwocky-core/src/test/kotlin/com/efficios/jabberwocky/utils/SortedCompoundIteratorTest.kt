/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.*

/**
 * Tests for the [SortedCompoundIterator]
 */
class SortedCompoundIteratorTest {

    companion object {

        private val list1 = Arrays.asList(1, 3, 5)
        private val list2 = Arrays.asList(2, 4, 6)
        private val list3 = Arrays.asList(4, 5, 6)
        private val list4 = Arrays.asList(2, 2, 2)

        private val customList1 = Arrays.asList(
                MyObject("A"),
                MyObject("B"),
                MyObject("C"))

        private val customList2 = Arrays.asList(
                MyObject("a"),
                MyObject("b"),
                MyObject("c"))
    }

    /**
     * Test that an error is correctly reported if there are less than 1
     * iterator.
     */
    @Test(expected = IllegalArgumentException::class)
    fun testNoIter() {
        SortedCompoundIterator(emptyList<Iterator<String>>(), Comparator.naturalOrder())
    }

    /**
     * Test a compound iterator with just 1 iterator, basically just a wrapper.
     */
    @Test
    fun test1Iter() {
        val iters = Arrays.asList<Iterator<Int>>(list1.iterator())
        val sci = SortedCompoundIterator(iters, Comparator.naturalOrder())
        testIteratorContents(sci, 1, 3, 5)
    }

    /**
     * Test a compound iterator with 2 iterators, requiring to sort the values.
     */
    @Test
    fun test2Iters() {
        val iters = Arrays.asList<Iterator<Int>>(list1.iterator(), list2.iterator())
        val sci = SortedCompoundIterator(iters, Comparator.naturalOrder())
        testIteratorContents(sci, 1, 2, 3, 4, 5, 6)
    }

    /**
     * Test that identical values coming from the same iterator are correctly
     * reported in the global iteration.
     */
    @Test
    fun testIdenticalValues1Iter() {
        val iters = Arrays.asList<Iterator<Int>>(list1.iterator(), list4.iterator())
        val sci = SortedCompoundIterator(iters, Comparator.naturalOrder())
        testIteratorContents(sci, 1, 2, 2, 2, 3, 5)
    }

    /**
     * Test a compound iterator with separate iterators that hold identical
     * (compareTo() == 0) values. The duplicate values should be present in the
     * iteration.
     */
    @Test
    fun testIdenticalValues2Iters() {
        val iters = Arrays.asList<Iterator<Int>>(list2.iterator(), list3.iterator())
        val sci = SortedCompoundIterator(iters, Comparator.naturalOrder())
        testIteratorContents(sci, 2, 4, 4, 5, 6, 6)
    }

    /**
     * Test that upon exhaustion, the iterator correctly throws a
     * [NoSuchElementException] if we try to read from it.
     */
    @Test(expected = NoSuchElementException::class)
    fun testExhaustion() {
        val iter1 = list1.iterator()
        val iters = Arrays.asList(iter1)
        val sci = SortedCompoundIterator(iters, Comparator.naturalOrder())

        sci.next()
        sci.next()
        sci.next()
        sci.next()
    }

    // ------------------------------------------------------------------------
    // Tests with custom objects and comparators
    // ------------------------------------------------------------------------

    private data class MyObject(val value: String) {}

    /**
     * Test a compound iterator of custom objects
     */
    @Test
    fun testCustomObjects1() {
        val iters = Arrays.asList<Iterator<MyObject>>(customList1.iterator(), customList2.iterator())
        val sci = SortedCompoundIterator(iters,
                Comparator.comparing<MyObject, String>({ it.value }))

        /* Default string comparator places capitals first */
        testIteratorContents(sci,
                MyObject("A"),
                MyObject("B"),
                MyObject("C"),
                MyObject("a"),
                MyObject("b"),
                MyObject("c"))
    }

    /**
     * Test a compound iterator of custom objects with a more complex
     * comparator.
     */
    @Test
    fun testCustomObjects2() {
        val iters = Arrays.asList<Iterator<MyObject>>(customList2.iterator(), customList1.iterator())
        val comparator = Comparator<MyObject> { o1, o2 -> o1.value.compareTo(o2.value, ignoreCase = true) }
        val sci = SortedCompoundIterator(iters, comparator)

        /*
         * Using custom comparator here. Note we put the contents of customList2
         * first, so they will show up first in the compound iterator.
         */
        testIteratorContents(sci,
                MyObject("a"),
                MyObject("A"),
                MyObject("b"),
                MyObject("B"),
                MyObject("c"),
                MyObject("C"))
    }

    @SafeVarargs
    private fun <T> testIteratorContents(iter: SortedCompoundIterator<T, out Iterator<T>>, vararg values: T) {
        for (value in values) {
            assertEquals(value, iter.next())
        }
        assertFalse(iter.hasNext())
    }

}
