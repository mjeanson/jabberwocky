/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace.event;

import java.util.Collections;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.efficios.jabberwocky.trace.event.ITraceLostEvent;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Range;

public class CtfTraceLostEvent extends CtfTraceEvent implements ITraceLostEvent {

    private final Range<Long> fTimeRange;
    private final long fNbLostEvents;

    public CtfTraceLostEvent(long startTime,
            long endTime,
            int cpu,
            String eventName,
            long nbLostEvents) {
        super(startTime, cpu, eventName, Collections.EMPTY_MAP, null);
        fTimeRange = Range.closed(startTime, endTime);
        fNbLostEvents = nbLostEvents;
    }

    @Override
    public Range<Long> getTimeRange() {
        return fTimeRange;
    }

    @Override
    public long getNbLostEvents() {
        return fNbLostEvents;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fTimeRange, fNbLostEvents);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        CtfTraceLostEvent other = (CtfTraceLostEvent) obj;
        return Objects.equals(fTimeRange, other.fTimeRange)
                && fNbLostEvents == other.fNbLostEvents;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("timerange", fTimeRange) //$NON-NLS-1$
                .add("event name", getEventName()) //$NON-NLS-1$
                .add("cpu", getCpu()) //$NON-NLS-1$
                .add("Nb Lost Events", fNbLostEvents)
                .toString();
    }
}
