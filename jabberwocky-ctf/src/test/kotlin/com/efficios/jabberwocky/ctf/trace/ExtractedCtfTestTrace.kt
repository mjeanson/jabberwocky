package com.efficios.jabberwocky.ctf.trace

import com.efficios.jabberwocky.trace.TraceInitializationException
import org.junit.rules.ExternalResource
import org.lttng.scope.ttt.ctf.CtfTestTrace
import java.nio.file.Paths

class ExtractedCtfTestTrace(private val testTrace: CtfTestTrace) : ExternalResource() {

    private lateinit var testTraceExtractor: CtfTestTraceExtractor
    lateinit var trace: CtfTrace

    override fun before() {
        testTraceExtractor = CtfTestTraceExtractor.extractTestTrace(testTrace)
        val tracePath = testTraceExtractor.trace.getPath()
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
