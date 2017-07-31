/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package com.efficios.jabberwocky.lttng.kernel.analysis.os;

import ca.polymtl.dorsal.libdelorean.IStateSystemWriter;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import ca.polymtl.dorsal.libdelorean.exceptions.StateValueTypeException;
import ca.polymtl.dorsal.libdelorean.exceptions.TimeRangeException;
import com.efficios.jabberwocky.analysis.statesystem.StateSystemAnalysis;
import com.efficios.jabberwocky.collection.ITraceCollection;
import com.efficios.jabberwocky.collection.TraceCollection;
import com.efficios.jabberwocky.lttng.kernel.analysis.os.handlers.*;
import com.efficios.jabberwocky.lttng.kernel.trace.LttngKernelTrace;
import com.efficios.jabberwocky.lttng.kernel.trace.layout.ILttngKernelEventLayout;
import com.efficios.jabberwocky.project.ITraceProject;
import com.efficios.jabberwocky.trace.ITrace;
import com.efficios.jabberwocky.trace.event.ITraceEvent;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * This is the state change input plugin for the state system which handles the
 * kernel traces.
 *
 * Attribute tree:
 *
 * <pre>
 * |- CPUs
 * |  |- <CPU number> -> CPU Status
 * |  |  |- CURRENT_THREAD
 * |  |  |- SOFT_IRQS
 * |  |  |  |- <Soft IRQ number> -> Soft IRQ Status
 * |  |  |- IRQS
 * |  |  |  |- <IRQ number> -> IRQ Status
 * |- THREADS
 * |  |- <Thread number> -> Thread Status
 * |  |  |- PPID
 * |  |  |- EXEC_NAME
 * |  |  |- PRIO
 * |  |  |- SYSTEM_CALL
 * |  |  |- CURRENT_CPU_RQ
 * </pre>
 *
 * @author Alexandre Montplaisir
 */
public class KernelAnalysis extends StateSystemAnalysis {

    private static final KernelAnalysis INSTANCE = new KernelAnalysis();

    public static KernelAnalysis instance() {
        return INSTANCE;
    }

    /**
     * Version number of this state provider. Please bump this if you modify the
     * contents of the generated state history in some way.
     */
    private static final int VERSION = 27;

    private KernelAnalysis() {}

    // ------------------------------------------------------------------------
    // IAnalysis
    // ------------------------------------------------------------------------

    @Override
    public boolean appliesTo(@Nullable ITraceProject<?, ?> project) {
        return (project != null && projectContainsKernelTrace(project));
    }

    @Override
    public boolean canExecute(@Nullable ITraceProject<?, ?> project) {
        return (project != null && projectContainsKernelTrace(project));
    }

    private static boolean projectContainsKernelTrace(ITraceProject<?, ?> project) {
        return project.getTraceCollections().stream()
                .flatMap(collection -> collection.getTraces().stream())
                .anyMatch(trace -> trace instanceof LttngKernelTrace);
    }

    // ------------------------------------------------------------------------
    // StateSystemAnalysis
    // ------------------------------------------------------------------------

    @Override
    public int getProviderVersion() {
        return VERSION;
    }

    @Override
    public ITraceCollection<?, ?> filterTraces(@Nullable ITraceProject<?, ?> project) {
        requireNonNull(project);
        Collection<LttngKernelTrace> kernelTraces = project.getTraceCollections().stream()
                .flatMap(collection -> collection.getTraces().stream())
                .filter(trace -> trace instanceof LttngKernelTrace)
                .map(trace -> (LttngKernelTrace) trace)
                .collect(Collectors.toList());
        return new TraceCollection(kernelTraces);
    }

    @Override
    public void handleEvent(IStateSystemWriter ss, ITraceEvent event, Object @Nullable [] trackedState) {
        ITrace trace = event.getTrace();
        if (!(trace instanceof LttngKernelTrace)) {
            /* We shouldn't have received this event... */
            return;
        }
        LttngKernelTrace kernelTrace = (LttngKernelTrace) trace;
        KernelAnalysisEventDefinitions defs = KernelAnalysisEventDefinitions.getDefsFromLayout(kernelTrace.getKernelEventLayout());

        final String eventName = event.getEventName();

        try {
            /*
             * Feed event to the history system if it's known to cause a state
             * transition.
             */
            KernelEventHandler handler = defs.getEventNames().get(eventName);
            if (handler == null) {
                if (isSyscallExit(defs.getLayout(), eventName)) {
                    handler = defs.getSysExitHandler();
                } else if (isSyscallEntry(defs.getLayout(), eventName)) {
                    handler = defs.getSysEntryHandler();
                }
            }
            if (handler != null) {
                handler.handleEvent(ss, event);
            }

        } catch (AttributeNotFoundException ae) {
            /*
             * This would indicate a problem with the logic of the manager here,
             * so it shouldn't happen.
             */
            // TODO Re-add logging
//            Activator.instance().logError("Attribute not found: " + ae.getMessage(), ae); //$NON-NLS-1$

        } catch (TimeRangeException tre) {
            /*
             * This would happen if the events in the trace aren't ordered
             * chronologically, which should never be the case ...
             */
            // TODO Re-add logging
//            Activator.instance().logError("TimeRangeExcpetion caught in the state system's event manager.\n" + //$NON-NLS-1$
//                    "Are the events in the trace correctly ordered?\n" + tre.getMessage(), tre); //$NON-NLS-1$

        } catch (StateValueTypeException sve) {
            /*
             * This would happen if we were trying to push/pop attributes not of
             * type integer. Which, once again, should never happen.
             */
            // TODO Re-add logging
//            Activator.instance().logError("State value error: " + sve.getMessage(), sve); //$NON-NLS-1$
        }
    }

    private static boolean isSyscallEntry(ILttngKernelEventLayout layout, String eventName) {
        return (eventName.startsWith(layout.eventSyscallEntryPrefix())
                || eventName.startsWith(layout.eventCompatSyscallEntryPrefix()));
    }

    private static boolean isSyscallExit(ILttngKernelEventLayout layout, String eventName) {
        return (eventName.startsWith(layout.eventSyscallExitPrefix())
                || eventName.startsWith(layout.eventCompatSyscallExitPrefix()));
    }

}
