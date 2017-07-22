/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.efficios.jabberwocky.lttng.kernel.analysis.os.handlers;

import static java.util.Objects.requireNonNull;

import com.efficios.jabberwocky.lttng.kernel.trace.layout.ILttngKernelEventLayout;

import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue;
import com.efficios.jabberwocky.trace.event.ITraceEvent;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystemBuilder;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.statevalue.TmfStateValue;

/**
 * IPI Exit Handler
 *
 * @author Matthew Khouzam
 */
public class IPIExitHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public IPIExitHandler(ILttngKernelEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITraceEvent event) throws AttributeNotFoundException {
        int cpu = event.getCpu();
        int currentThreadNode = KernelEventHandlerUtils.getCurrentThreadNode(cpu, ss);
        Long irqId = requireNonNull(event.getField(getLayout().fieldIPIVector(), IntegerValue.class)).getValue();

        /* Put this IRQ back to inactive in the resource tree */
        int quark = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeIRQs(cpu, ss), irqId.toString());
        TmfStateValue value = TmfStateValue.nullValue();
        long timestamp = event.getTimestamp();
        ss.modifyAttribute(timestamp, value, quark);

        /* Set the previous process back to running */
        KernelEventHandlerUtils.setProcessToRunning(timestamp, currentThreadNode, ss);

        /* Set the CPU status back to running or "idle" */
        KernelEventHandlerUtils.cpuExitInterrupt(timestamp, cpu, ss);
    }
}
