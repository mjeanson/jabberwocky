/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.render.arrows;

import com.efficios.jabberwocky.views.timegraph.model.render.TimeGraphEvent;

public class TimeGraphArrow {

    private final TimeGraphEvent fStartEvent;
    private final TimeGraphEvent fEndEvent;

    public TimeGraphArrow(TimeGraphEvent startEvent, TimeGraphEvent endEvent) {
        fStartEvent = startEvent;
        fEndEvent = endEvent;
    }

    public TimeGraphEvent getStartEvent() {
        return fStartEvent;
    }

    public TimeGraphEvent getEndEvent() {
        return fEndEvent;
    }

}
