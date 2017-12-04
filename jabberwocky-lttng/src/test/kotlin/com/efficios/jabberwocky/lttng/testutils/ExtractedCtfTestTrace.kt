/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.testutils

import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.trace.TraceInitializationException
import org.junit.rules.ExternalResource
import org.lttng.scope.ttt.ctf.CtfTestTrace
import java.nio.file.Paths

class ExtractedCtfTestTrace(private val testTrace: CtfTestTrace) : ExternalResource() {

    private lateinit var testTraceExtractor: CtfTestTraceExtractor
    lateinit var trace: CtfTrace

    override fun before() {
        testTraceExtractor = CtfTestTraceExtractor.extractTestTrace(testTrace)
        val tracePath = testTraceExtractor.trace.path
        try {
            trace = CtfTrace(Paths.get(tracePath))
        } catch (e: TraceInitializationException) {
            throw IllegalArgumentException(e)
        }

    }

    override fun after() {
        testTraceExtractor.close()
    }

}
