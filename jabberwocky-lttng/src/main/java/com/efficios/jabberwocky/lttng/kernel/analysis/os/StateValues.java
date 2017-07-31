/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package com.efficios.jabberwocky.lttng.kernel.analysis.os;

import ca.polymtl.dorsal.libdelorean.statevalue.IStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;

/**
 * State values that are used in the kernel event handler. It's much better to
 * use integer values whenever possible, since those take much less space in the
 * history file.
 *
 * @author Alexandre Montplaisir
 * @noimplement This interface is not intended to be implemented by clients.
 */
@SuppressWarnings("javadoc")
public interface StateValues {

    /* Process status */
    int PROCESS_STATUS_UNKNOWN = 0;
    int PROCESS_STATUS_WAIT_BLOCKED = 1;
    int PROCESS_STATUS_RUN_USERMODE = 2;
    int PROCESS_STATUS_RUN_SYSCALL = 3;
    int PROCESS_STATUS_INTERRUPTED = 4;
    int PROCESS_STATUS_WAIT_FOR_CPU = 5;
    int PROCESS_STATUS_WAIT_UNKNOWN = 6;

    IStateValue PROCESS_STATUS_UNKNOWN_VALUE = StateValue.newValueInt(PROCESS_STATUS_UNKNOWN);
    IStateValue PROCESS_STATUS_WAIT_UNKNOWN_VALUE = StateValue.newValueInt(PROCESS_STATUS_WAIT_UNKNOWN);
    IStateValue PROCESS_STATUS_WAIT_BLOCKED_VALUE = StateValue.newValueInt(PROCESS_STATUS_WAIT_BLOCKED);
    IStateValue PROCESS_STATUS_RUN_USERMODE_VALUE = StateValue.newValueInt(PROCESS_STATUS_RUN_USERMODE);
    IStateValue PROCESS_STATUS_RUN_SYSCALL_VALUE = StateValue.newValueInt(PROCESS_STATUS_RUN_SYSCALL);
    IStateValue PROCESS_STATUS_INTERRUPTED_VALUE = StateValue.newValueInt(PROCESS_STATUS_INTERRUPTED);
    IStateValue PROCESS_STATUS_WAIT_FOR_CPU_VALUE = StateValue.newValueInt(PROCESS_STATUS_WAIT_FOR_CPU);

    /* CPU Status */
    int CPU_STATUS_IDLE = 0;
    /**
     * Soft IRQ raised, could happen in the CPU attribute but should not since
     * this means that the CPU went idle when a softirq was raised.
     */
    int CPU_STATUS_SOFT_IRQ_RAISED = (1 << 0);
    int CPU_STATUS_RUN_USERMODE = (1 << 1);
    int CPU_STATUS_RUN_SYSCALL = (1 << 2);
    int CPU_STATUS_SOFTIRQ = (1 << 3);
    int CPU_STATUS_IRQ = (1 << 4);

    IStateValue CPU_STATUS_IDLE_VALUE = StateValue.newValueInt(CPU_STATUS_IDLE);
    IStateValue CPU_STATUS_RUN_USERMODE_VALUE = StateValue.newValueInt(CPU_STATUS_RUN_USERMODE);
    IStateValue CPU_STATUS_RUN_SYSCALL_VALUE = StateValue.newValueInt(CPU_STATUS_RUN_SYSCALL);
    IStateValue CPU_STATUS_IRQ_VALUE = StateValue.newValueInt(CPU_STATUS_IRQ);
    IStateValue CPU_STATUS_SOFTIRQ_VALUE = StateValue.newValueInt(CPU_STATUS_SOFTIRQ);

    /** Soft IRQ is raised, CPU is in user mode */
    IStateValue SOFT_IRQ_RAISED_VALUE = StateValue.newValueInt(CPU_STATUS_SOFT_IRQ_RAISED);

    /** If the softirq is running and another is raised at the same time. */
    IStateValue SOFT_IRQ_RAISED_RUNNING_VALUE = StateValue.newValueInt(CPU_STATUS_SOFT_IRQ_RAISED | CPU_STATUS_SOFTIRQ);
}
