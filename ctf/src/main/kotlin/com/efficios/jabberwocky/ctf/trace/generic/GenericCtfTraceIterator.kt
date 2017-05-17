package com.efficios.jabberwocky.ctf.trace.generic

import com.efficios.jabberwocky.ctf.trace.CtfTraceIterator
import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent

class GenericCtfTraceIterator(originTrace: GenericCtfTrace) : CtfTraceIterator<CtfTraceEvent>(originTrace)
