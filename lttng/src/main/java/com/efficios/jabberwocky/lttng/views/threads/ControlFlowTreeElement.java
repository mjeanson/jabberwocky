/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.views.threads;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.efficios.jabberwocky.views.timegraph.model.provider.statesystem.StateSystemTimeGraphTreeElement;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;

public class ControlFlowTreeElement extends StateSystemTimeGraphTreeElement {

    private static final String UNKNOWN_THREAD_NAME = "???"; //$NON-NLS-1$

    private final int fTid;
    private final String fThreadName;

    public ControlFlowTreeElement(String tidStr, @Nullable String threadName,
            List<TimeGraphTreeElement> children, int sourceQuark) {
        super(getElementName(tidStr, threadName),
                children,
                sourceQuark);

        if (tidStr.startsWith(Attributes.THREAD_0_PREFIX)) {
            fTid = 0;
        } else {
            fTid = Integer.parseInt(tidStr);
        }

        fThreadName = (threadName == null ? UNKNOWN_THREAD_NAME : threadName);
    }

    private static String getElementName(String tidStr, @Nullable String threadName) {
        String tidPart = tidStr;
        if (tidPart.startsWith(Attributes.THREAD_0_PREFIX)) {
            /* Display "0/0" instead of "0_0" */
            tidPart = tidPart.replace('_', '/');
        }

        String threadNamePart = (threadName == null ? UNKNOWN_THREAD_NAME : threadName);
        return (tidPart + " - " + threadNamePart); //$NON-NLS-1$
    }

    public int getTid() {
        return fTid;
    }

    public String getThreadName() {
        return fThreadName;
    }

}
