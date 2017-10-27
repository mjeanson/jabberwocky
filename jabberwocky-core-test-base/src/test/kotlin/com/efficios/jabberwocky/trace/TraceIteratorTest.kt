/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace

class TraceIteratorTest : TraceIteratorTestBase() {

    override val trace = TraceStubs.TraceStub1()

    override val event1 = trace.events[0]
    override val event2 = trace.events[1]
    override val event3 = trace.events[2]
    override val lastEvent = trace.events.last()

    override val timestampBetween1and2 = 3L
    override val timestampAfterEnd = 12L

}
