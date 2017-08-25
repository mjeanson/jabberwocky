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
import com.efficios.jabberwocky.lttng.kernel.analysis.os.Attributes;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.LinuxValues;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.StateValues;
import com.efficios.jabberwocky.lttng.kernel.trace.layout.ILttngKernelEventLayout;
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue;
import com.efficios.jabberwocky.trace.event.FieldValue.StringValue;
import com.efficios.jabberwocky.trace.event.ITraceEvent;
import org.eclipse.jdt.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * LTTng Specific state dump event handler
 */
public class StateDumpHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public StateDumpHandler(ILttngKernelEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(IStateSystemWriter ss, ITraceEvent event) throws AttributeNotFoundException {
        int eventCpu = event.getCpu();
        Long tid = requireNonNull(event.getField("tid", IntegerValue.class)).getValue(); //$NON-NLS-1$
        Long pid =  requireNonNull(event.getField("pid", IntegerValue.class)).getValue(); //$NON-NLS-1$
        Long ppid = requireNonNull(event.getField("ppid", IntegerValue.class)).getValue(); //$NON-NLS-1$
        Long status = requireNonNull(event.getField("status", IntegerValue.class)).getValue(); //$NON-NLS-1$
        String name = requireNonNull(event.getField("name", StringValue.class)).getValue(); //$NON-NLS-1$

        /* Only present in LTTng 2.10+ */
        IntegerValue cpuFieldValue = event.getField("cpu", IntegerValue.class); //$NON-NLS-1$
        @Nullable Long cpuField = (cpuFieldValue == null ? null : cpuFieldValue.getValue());

        /*
         * "mode" could be interesting too, but it doesn't seem to be populated
         * with anything relevant for now.
         */

        String threadAttributeName = Attributes.buildThreadAttributeName(tid.intValue(), eventCpu);
        if (threadAttributeName == null) {
            return;
        }

        int curThreadNode = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeThreads(ss), threadAttributeName);
        long timestamp = event.getTimestamp();
        /* Set the process' name */
        setProcessName(ss, name, curThreadNode, timestamp);

        /* Set the process' PPID */
        setPpid(ss, tid.intValue(), pid.intValue(), ppid.intValue(), curThreadNode, timestamp);

        /* Set the process' status */
        setStatus(ss, status.intValue(), curThreadNode, cpuField, timestamp);
    }

    private static void setStatus(IStateSystemWriter ss, int status, int curThreadNode, @Nullable Long cpu, long timestamp) {
        StateValue value;
        if (ss.queryOngoingState(curThreadNode).isNull()) {
            switch (status) {
            case LinuxValues.STATEDUMP_PROCESS_STATUS_WAIT_CPU:
                value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
                setRunQueue(ss, curThreadNode, cpu, timestamp);
                break;
            case LinuxValues.STATEDUMP_PROCESS_STATUS_WAIT:
                /*
                 * We have no information on what the process is waiting on
                 * (unlike a sched_switch for example), so we will use the
                 * WAIT_UNKNOWN state instead of the "normal" WAIT_BLOCKED
                 * state.
                 */
                value = StateValues.PROCESS_STATUS_WAIT_UNKNOWN_VALUE;
                break;
            default:
                value = StateValues.PROCESS_STATUS_UNKNOWN_VALUE;
            }
            ss.modifyAttribute(timestamp, value, curThreadNode);
        }
    }

    private static void setRunQueue(IStateSystemWriter ss, int curThreadNode, @Nullable Long cpu, long timestamp) {
        if (cpu != null) {
            int quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.CURRENT_CPU_RQ);
            StateValue value = StateValue.newValueInt(cpu.intValue());
            ss.modifyAttribute(timestamp, value, quark);
        }
    }

    private static void setPpid(IStateSystemWriter ss, int tid, int pid, int ppid, int curThreadNode, long timestamp)
            throws AttributeNotFoundException {
        StateValue value;
        int quark;
        quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.PPID);
        if (ss.queryOngoingState(quark).isNull()) {
            if (pid == tid) {
                /* We have a process. Use the 'PPID' field. */
                value = StateValue.newValueInt(ppid);
            } else {
                /* We have a thread, use the 'PID' field for the parent. */
                value = StateValue.newValueInt(pid);
            }
            ss.modifyAttribute(timestamp, value, quark);
        }
    }

    private static void setProcessName(IStateSystemWriter ss, String name, int curThreadNode, long timestamp)
            throws AttributeNotFoundException {
        StateValue value;
        int quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.EXEC_NAME);
        if (ss.queryOngoingState(quark).isNull()) {
            /* If the value didn't exist previously, set it */
            value = StateValue.newValueString(name);
            ss.modifyAttribute(timestamp, value, quark);
        }
    }
}
