package com.efficios.jabberwocky.ctf.trace.generic;

import com.efficios.jabberwocky.ctf.trace.CtfTraceIterator;
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent;

public class GenericCtfTraceIterator extends CtfTraceIterator<CtfTraceEvent> {

    public GenericCtfTraceIterator(GenericCtfTrace originTrace) {
        super(originTrace);
    }

}
