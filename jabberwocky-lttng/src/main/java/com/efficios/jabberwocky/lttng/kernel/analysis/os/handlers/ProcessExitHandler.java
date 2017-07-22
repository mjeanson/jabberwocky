/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package com.efficios.jabberwocky.lttng.kernel.analysis.os.handlers;

import com.efficios.jabberwocky.lttng.kernel.trace.layout.ILttngKernelEventLayout;

import com.efficios.jabberwocky.trace.event.ITraceEvent;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystemBuilder;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;

/**
 * Process Exit handler
 */
public class ProcessExitHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public ProcessExitHandler(ILttngKernelEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITraceEvent event) throws AttributeNotFoundException {
        /* No state modifications tracked atm */
    }

}
