/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.analysis.os

import com.efficios.jabberwocky.lttng.kernel.analysis.os.handlers.*
import com.efficios.jabberwocky.lttng.kernel.trace.layout.ILttngKernelEventLayout

class KernelAnalysisEventDefinitions private constructor(val layout: ILttngKernelEventLayout) {

    companion object {

        private val DEFINITIONS_MAP: MutableMap<ILttngKernelEventLayout, KernelAnalysisEventDefinitions> = mutableMapOf()

        @JvmStatic
        @Synchronized
        fun getDefsFromLayout(layout: ILttngKernelEventLayout): KernelAnalysisEventDefinitions {
            var definitions = DEFINITIONS_MAP[layout]
            if (definitions == null) {
                definitions = KernelAnalysisEventDefinitions(layout)
                DEFINITIONS_MAP[layout] = definitions
            }
            return definitions
        }
    }

    val sysEntryHandler: KernelEventHandler = SysEntryHandler(layout)
    val sysExitHandler: KernelEventHandler = SysExitHandler(layout)
    val eventNames: Map<String, KernelEventHandler>

    init {
        val map = mutableMapOf(Pair(layout.eventIrqHandlerEntry(), IrqEntryHandler(layout)),
                Pair(layout.eventIrqHandlerExit(), IrqExitHandler(layout)),
                Pair(layout.eventSoftIrqEntry(), SoftIrqEntryHandler(layout)),
                Pair(layout.eventSoftIrqExit(), SoftIrqExitHandler(layout)),
                Pair(layout.eventSoftIrqRaise(), SoftIrqRaiseHandler(layout)),
                Pair(layout.eventSchedSwitch(), SchedSwitchHandler(layout)),
                Pair(layout.eventSchedPiSetprio(), PiSetprioHandler(layout)),
                Pair(layout.eventSchedProcessFork(), ProcessForkHandler(layout)),
                Pair(layout.eventSchedProcessExit(), ProcessExitHandler(layout)),
                Pair(layout.eventSchedProcessFree(), ProcessFreeHandler(layout)),
                Pair(layout.eventSchedProcessWaking(), SchedWakeupHandler(layout)),
                Pair(layout.eventSchedMigrateTask(), SchedMigrateTaskHandler(layout)))

        layout.ipiIrqVectorsEntries.forEach { map.put(it, IPIEntryHandler(layout)) }
        layout.ipiIrqVectorsExits.forEach { map.put(it, IPIExitHandler(layout)) }

        val eventStatedumpProcessState = layout.eventStatedumpProcessState()
        if (eventStatedumpProcessState != null) map.put(eventStatedumpProcessState, StateDumpHandler(layout))

        layout.eventsSchedWakeup().forEach { map.put(it, SchedWakeupHandler(layout)) }

        eventNames = map
    }
}