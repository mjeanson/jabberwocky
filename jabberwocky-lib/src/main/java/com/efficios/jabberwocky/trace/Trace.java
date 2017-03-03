package com.efficios.jabberwocky.trace;

import org.eclipse.jdt.annotation.Nullable;

import com.efficios.jabberwocky.trace.event.TraceEvent;

public abstract class Trace<E extends TraceEvent> implements ITrace<E> {

    private transient @Nullable Long fStartTime = null;

    protected Trace() {}

    @Override
    public synchronized long getStartTime() {
        Long startTime = fStartTime;
        if (startTime != null) {
            return startTime.longValue();
        }
        /*
         * Lazy-load the start time by reading the timestamp of the first event.
         */
        try (ITraceIterator<E> iter = getIterator()) {
            if (iter.hasNext()) {
                startTime = Long.valueOf(iter.next().getTimestamp());
            } else {
                /* Empty trace */
                startTime = Long.valueOf(0L);
            }
        }
        fStartTime = startTime;
        return startTime.longValue();
    }

}
