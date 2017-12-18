/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package com.efficios.jabberwocky.lttng.kernel.analysis.os.handlers;

import ca.polymtl.dorsal.libdelorean.IStateSystemWriter;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.Attributes;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.StateValues;
import com.efficios.jabberwocky.lttng.kernel.trace.layout.ILttngKernelEventLayout;
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue;
import com.efficios.jabberwocky.trace.event.TraceEvent;

/**
 * Handler for task migration events. Normally moves a (non-running) process
 * from one run queue to another.
 *
 * @author Alexandre Montplaisir
 */
public class SchedMigrateTaskHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            The event layout to use
     */
    public SchedMigrateTaskHandler(ILttngKernelEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(IStateSystemWriter ss, TraceEvent event) throws AttributeNotFoundException {
        IntegerValue tidField = (IntegerValue) event.getFields().get(getLayout().fieldTid());
        IntegerValue destCpuField = (IntegerValue) event.getFields().get(getLayout().fieldDestCpu());
        if (tidField == null || destCpuField == null) {
            return;
        }

        Long tid = tidField.getValue();
        Long destCpu = destCpuField.getValue();
        long t = event.getTimestamp();

        String threadAttributeName = Attributes.buildThreadAttributeName(tid.intValue(), null);
        if (threadAttributeName == null) {
            /* Swapper threads do not get migrated */
            return;
        }
        int threadNode = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeThreads(ss), threadAttributeName);

        /*
         * Put the thread in the "wait for cpu" state. Some older versions of
         * the kernel/tracers may not have the corresponding sched_waking events
         * that also does so, so we can set it at the migrate, if applicable.
         */
        StateValue value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
        ss.modifyAttribute(t, value, threadNode);

        /* Update the thread's running queue to the new one indicated by the event */
        int quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.CURRENT_CPU_RQ);
        value = StateValue.newValueInt(destCpu.intValue());
        ss.modifyAttribute(t, value, quark);
    }

}
