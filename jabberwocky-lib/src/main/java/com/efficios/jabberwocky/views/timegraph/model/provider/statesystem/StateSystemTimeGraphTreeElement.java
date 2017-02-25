/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.provider.statesystem;

import java.util.List;

import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement;

public class StateSystemTimeGraphTreeElement extends TimeGraphTreeElement {

    private final int fSourceQuark;

    public StateSystemTimeGraphTreeElement(String name,
            List<TimeGraphTreeElement> children,
            int sourceQuark) {
        super(name, children);
        fSourceQuark = sourceQuark;
    }

    public int getSourceQuark() {
        return fSourceQuark;
    }

}
