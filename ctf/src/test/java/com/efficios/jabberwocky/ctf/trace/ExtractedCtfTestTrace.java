package com.efficios.jabberwocky.ctf.trace;

import java.nio.file.Paths;

import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.rules.ExternalResource;

import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTrace;
import com.efficios.jabberwocky.trace.TraceInitializationException;

public class ExtractedCtfTestTrace extends ExternalResource {

    private final CtfTestTrace fTestTrace;

    private CtfTestTraceExtractor testTraceExtractor;
    private GenericCtfTrace fTrace;

    public ExtractedCtfTestTrace(CtfTestTrace testTrace) {
        fTestTrace = testTrace;
    }

    @Override
    protected void before() {
        testTraceExtractor = CtfTestTraceExtractor.extractTestTrace(fTestTrace);
        String tracePath = testTraceExtractor.getTrace().getPath();
        try {
            fTrace = new GenericCtfTrace(Paths.get(tracePath));
        } catch (TraceInitializationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected void after() {
        testTraceExtractor.close();
    }

    public GenericCtfTrace getTrace() {
        return fTrace;
    }

}
