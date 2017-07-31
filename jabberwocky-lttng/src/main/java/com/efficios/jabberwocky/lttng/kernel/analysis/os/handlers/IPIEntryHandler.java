/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.efficios.jabberwocky.lttng.kernel.analysis.os.handlers;

import ca.polymtl.dorsal.libdelorean.IStateSystemWriter;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.statevalue.IStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.StateValues;
import com.efficios.jabberwocky.lttng.kernel.trace.layout.ILttngKernelEventLayout;
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue;
import com.efficios.jabberwocky.trace.event.ITraceEvent;

import static java.util.Objects.requireNonNull;

/**
 * IPI Entry Handler
 *
 * @author Matthew Khouzam
 */
public class IPIEntryHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public IPIEntryHandler(ILttngKernelEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(IStateSystemWriter ss, ITraceEvent event) throws AttributeNotFoundException {

        int cpu = event.getCpu();
        Long irqId = requireNonNull(event.getField(getLayout().fieldIPIVector(), IntegerValue.class)).getValue();

        /*
         * Mark this IRQ as active in the resource tree. The state value = the
         * CPU on which this IRQ is sitting
         */
        int quark = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeIRQs(cpu, ss), irqId.toString());

        IStateValue value = StateValue.newValueInt(cpu);
        long timestamp = event.getTimestamp();
        ss.modifyAttribute(timestamp, value, quark);

        /* Change the status of the running process to interrupted */
        quark = KernelEventHandlerUtils.getCurrentThreadNode(cpu, ss);
        value = StateValues.PROCESS_STATUS_INTERRUPTED_VALUE;
        ss.modifyAttribute(timestamp, value, quark);

        /* Change the status of the CPU to interrupted */
        quark = KernelEventHandlerUtils.getCurrentCPUNode(cpu, ss);
        value = StateValues.CPU_STATUS_IRQ_VALUE;
        ss.modifyAttribute(timestamp, value, quark);
    }

}
