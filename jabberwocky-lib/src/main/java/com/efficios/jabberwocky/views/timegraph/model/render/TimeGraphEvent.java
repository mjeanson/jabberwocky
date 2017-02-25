/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.render;

import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;

public class TimeGraphEvent {

    private final long fTimestamp;
    private final TimeGraphTreeElement fTreeElement;

    public TimeGraphEvent(long timestamp, TimeGraphTreeElement treeElement) {
        fTimestamp = timestamp;
        fTreeElement = treeElement;
    }

    public long getTimestamp() {
        return fTimestamp;
    }

    public TimeGraphTreeElement getTreeElement() {
        return fTreeElement;
    }
}
