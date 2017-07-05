package com.efficios.jabberwocky.lttng.trace

import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTrace
import com.efficios.jabberwocky.trace.TraceInitializationException
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace
import org.junit.rules.ExternalResource
import java.nio.file.Paths

class ExtractedCtfTestTrace(private val testTrace: CtfTestTrace) : ExternalResource() {

    private lateinit var testTraceExtractor: CtfTestTraceExtractor
    lateinit var trace: GenericCtfTrace

    override fun before() {
        testTraceExtractor = CtfTestTraceExtractor.extractTestTrace(testTrace)
        val tracePath = testTraceExtractor.trace.getPath()
        try {
            trace = GenericCtfTrace(Paths.get(tracePath))
        } catch (e: TraceInitializationException) {
            throw IllegalArgumentException(e)
        }

    }

    override fun after() {
        testTraceExtractor.close()
    }

}
