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

import ca.polymtl.dorsal.libdelorean.IStateSystemWriter;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.StateValues;
import com.efficios.jabberwocky.lttng.kernel.trace.layout.ILttngKernelEventLayout;
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue;
import com.efficios.jabberwocky.trace.event.TraceEvent;

import static java.util.Objects.requireNonNull;

/**
 * Soft Irq Entry handler
 */
public class SoftIrqEntryHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public SoftIrqEntryHandler(ILttngKernelEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(IStateSystemWriter ss, TraceEvent event) throws AttributeNotFoundException {
        int cpu = event.getCpu();
        long timestamp = event.getTimestamp();
        Long softIrqId = ((IntegerValue) event.getFields().get(getLayout().fieldVec())).getValue();
        int currentCPUNode = KernelEventHandlerUtils.getCurrentCPUNode(cpu, ss);
        int currentThreadNode = KernelEventHandlerUtils.getCurrentThreadNode(cpu, ss);

        /*
         * Mark this SoftIRQ as active in the resource tree.
         */
        int quark = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeSoftIRQs(cpu, ss), softIrqId.toString());
        StateValue value = StateValues.CPU_STATUS_SOFTIRQ_VALUE;
        ss.modifyAttribute(timestamp, value, quark);

        /* Change the status of the running process to interrupted */
        value = StateValues.PROCESS_STATUS_INTERRUPTED_VALUE;
        ss.modifyAttribute(timestamp, value, currentThreadNode);

        /* Change the status of the CPU to interrupted */
        value = StateValues.CPU_STATUS_SOFTIRQ_VALUE;
        ss.modifyAttribute(timestamp, value, currentCPUNode);
    }
}
