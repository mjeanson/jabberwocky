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
import ca.polymtl.dorsal.libdelorean.statevalue.IntegerStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.StateValues;
import com.efficios.jabberwocky.lttng.kernel.trace.layout.ILttngKernelEventLayout;
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue;
import com.efficios.jabberwocky.trace.event.TraceEvent;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Raise a soft irq event
 */
public class SoftIrqRaiseHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public SoftIrqRaiseHandler(ILttngKernelEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(IStateSystemWriter ss, TraceEvent event) throws AttributeNotFoundException {
        int cpu = event.getCpu();
        Long softIrqId = ((IntegerValue) event.getFields().get(getLayout().fieldVec())).getValue();

        /*
         * Mark this SoftIRQ as *raised* in the resource tree.
         */
        int quark = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeSoftIRQs(cpu, ss), softIrqId.toString());

        StateValue value = (isInSoftirq(ss.queryOngoingState(quark)) ?
                StateValues.SOFT_IRQ_RAISED_RUNNING_VALUE :
                StateValues.SOFT_IRQ_RAISED_VALUE);
        ss.modifyAttribute(event.getTimestamp(), value, quark);

    }

    private static boolean isInSoftirq(@Nullable StateValue state) {
        return (state != null &&
                !state.isNull() &&
                (((IntegerStateValue) state).getValue() & StateValues.CPU_STATUS_SOFTIRQ) == StateValues.CPU_STATUS_SOFTIRQ);
    }
}
