package test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;

import com.efficios.jabberwocky.ctf.trace.CtfTrace;
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent;
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTrace;
import com.efficios.jabberwocky.trace.ITraceIterator;
import com.efficios.jabberwocky.trace.TraceInitializationException;
import com.efficios.jabberwocky.trace.event.ITraceEvent;
import com.efficios.jabberwocky.trace.event.field.IFieldValue;

public class TestCtf {

    private static final String TRACE_PATH = "/home/alexandre/src/tc/tracecompass-test-traces/ctf/src/main/resources/kernel";

    private static final TmfTimestampFormat TS_FORMAT = new TmfTimestampFormat(TmfTimestampFormat.DEFAULT_TIME_PATTERN);

    public static void main(String[] args) throws TraceInitializationException {
        Path tracePath = Paths.get(TRACE_PATH);
        CtfTrace<CtfTraceEvent> trace = new GenericCtfTrace(tracePath);
        try (ITraceIterator<CtfTraceEvent> iter = trace.getIterator()) {
            long prevTimestamp = 0;
            for (int i = 0; iter.hasNext(); i++) {
                ITraceEvent event = iter.next();
                long offset = event.getTimestamp() - prevTimestamp;
                printEvent(trace, event, offset);
                prevTimestamp = event.getTimestamp();
            }
        }
    }

    private static void printEvent(CtfTrace<CtfTraceEvent> trace, ITraceEvent event, long offset) {
        long ts = event.getTimestamp();
        long ts2 = trace.getInnerTrace().timestampCyclesToNanos(ts);
        // String timestampStr = String.valueOf(ts2);
        String timestampStr = TS_FORMAT.format(ts2);

        StringJoiner sj = new StringJoiner(", ")
                .add(String.valueOf(timestampStr))
                .add(String.valueOf(offset))
                .add(event.getEventName())
                .add(String.valueOf(event.getCpu()));

        StringJoiner sj2 = new StringJoiner(", ", "{", "}");
        for (String fieldName : event.getFieldNames()) {
            IFieldValue fieldValue = event.getField(fieldName, IFieldValue.class);
            sj2.add(fieldName + '=' + fieldValue.toString());
        }
        sj.add(sj2.toString());

        System.out.println(sj.toString());
    }

}
