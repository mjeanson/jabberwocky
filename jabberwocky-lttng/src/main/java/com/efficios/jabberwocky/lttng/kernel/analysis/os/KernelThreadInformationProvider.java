/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package com.efficios.jabberwocky.lttng.kernel.analysis.os;

import org.eclipse.jdt.annotation.Nullable;

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import ca.polymtl.dorsal.libdelorean.interval.ITmfStateInterval;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue;
import ca.polymtl.dorsal.libdelorean.statevalue.ITmfStateValue.Type;

/**
 * Information provider utility class that retrieves thread-related information
 * from a Linux Kernel Analysis
 *
 * @author Geneviève Bastien
 */
public final class KernelThreadInformationProvider {

    private KernelThreadInformationProvider() {
    }

    /**
     * Get the ID of the thread running on the CPU at time ts
     *
     * TODO: This method may later be replaced by an aspect, when the aspect can
     * resolve to something that is not an event
     *
     * @param module
     *            The kernel analysis instance to run this method on
     * @param cpuId
     *            The CPU number the process is running on
     * @param ts
     *            The timestamp at which we want the running process
     * @return The TID of the thread running on CPU cpuId at time ts or
     *         {@code null} if either no thread is running or we do not know.
     */
    public static @Nullable Integer getThreadOnCpu(ITmfStateSystem ss, long cpuId, long ts) {
        int cpuQuark = ss.getQuarkAbsolute(Attributes.CPUS, Long.toString(cpuId), Attributes.CURRENT_THREAD);
        ITmfStateInterval interval = ss.querySingleState(ts, cpuQuark);
        ITmfStateValue val = interval.getStateValue();
        if (val.getType() == Type.INTEGER) {
            return val.unboxInt();
        }
        return null;
    }
//
//    /**
//     * Get the TIDs of the threads from an analysis
//     *
//     * @param module
//     *            The kernel analysis instance to run this method on
//     * @return The set of TIDs corresponding to the threads
//     */
//    public static Collection<Integer> getThreadIds(KernelAnalysisModule module) {
//        ITmfStateSystem ss = module.getStateSystem();
//        if (ss == null) {
//            return Collections.EMPTY_SET;
//        }
//        int threadQuark;
//        try {
//            threadQuark = ss.getQuarkAbsolute(Attributes.THREADS);
//            Set<@NonNull Integer> tids = new TreeSet<>();
//            for (Integer quark : ss.getSubAttributes(threadQuark, false)) {
//                final @NonNull String attributeName = ss.getAttributeName(quark);
//                tids.add(attributeName.startsWith(Attributes.THREAD_0_PREFIX) ? 0 : Integer.parseInt(attributeName));
//            }
//            return tids;
//        } catch (AttributeNotFoundException e) {
//        }
//        return Collections.EMPTY_SET;
//    }
//
//    /**
//     * Get the parent process ID of a thread
//     *
//     * @param module
//     *            The kernel analysis instance to run this method on
//     * @param threadId
//     *            The thread ID of the process for which to get the parent
//     * @param ts
//     *            The timestamp at which to get the parent
//     * @return The parent PID or {@code null} if the PPID is not found.
//     */
//    public static @Nullable Integer getParentPid(KernelAnalysisModule module, Integer threadId, long ts) {
//        ITmfStateSystem ss = module.getStateSystem();
//        if (ss == null) {
//            return null;
//        }
//        Integer ppidNode;
//        try {
//            ppidNode = ss.getQuarkAbsolute(Attributes.THREADS, threadId.toString(), Attributes.PPID);
//            ITmfStateInterval ppidInterval = ss.querySingleState(ts, ppidNode);
//            ITmfStateValue ppidValue = ppidInterval.getStateValue();
//
//            if (ppidValue.getType().equals(Type.INTEGER)) {
//                return Integer.valueOf(ppidValue.unboxInt());
//            }
//        } catch (AttributeNotFoundException | StateSystemDisposedException | TimeRangeException e) {
//        }
//        return null;
//    }
//
//    /**
//     * Get the executable name of the thread ID. If the thread ID was used
//     * multiple time or the name changed in between, it will return the last
//     * name the thread has taken, or {@code null} if no name is found
//     *
//     * @param module
//     *            The kernel analysis instance to run this method on
//     * @param threadId
//     *            The thread ID of the process for which to get the name
//     * @return The last executable name of this process, or {@code null} if not
//     *         found
//     */
//    public static @Nullable String getExecutableName(KernelAnalysisModule module, Integer threadId) {
//        ITmfStateSystem ss = module.getStateSystem();
//        if (ss == null) {
//            return null;
//        }
//        try {
//            Integer  execNameNode = ss.getQuarkAbsolute(Attributes.THREADS, threadId.toString(), Attributes.EXEC_NAME);
//            List<ITmfStateInterval> execNameIntervals = StateSystemUtils.queryHistoryRange(ss, execNameNode, ss.getStartTime(), ss.getCurrentEndTime());
//
//            ITmfStateValue execNameValue;
//            String execName = null;
//            for (ITmfStateInterval interval : execNameIntervals) {
//                execNameValue = interval.getStateValue();
//                if (execNameValue.getType().equals(Type.STRING)) {
//                    execName = execNameValue.unboxStr();
//                }
//            }
//            return execName;
//        } catch (AttributeNotFoundException | StateSystemDisposedException | TimeRangeException e) {
//        }
//        return null;
//    }
//
//    /**
//     * Get the priority of this thread at time ts
//     *
//     * @param module
//     *            The kernel analysis instance to run this method on
//     * @param threadId
//     *            The ID of the thread to query
//     * @param ts
//     *            The timestamp at which to query
//     * @return The priority of the thread or <code>-1</code> if not available
//     */
//    public static int getThreadPriority(KernelAnalysisModule module, int threadId, long ts) {
//        ITmfStateSystem ss = module.getStateSystem();
//        if (ss == null) {
//            return -1;
//        }
//        try {
//            int prioQuark = ss.getQuarkAbsolute(Attributes.THREADS, String.valueOf(threadId), Attributes.PRIO);
//            return ss.querySingleState(ts, prioQuark).getStateValue().unboxInt();
//        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
//            return -1;
//        }
//    }
//    /**
//     * Get the status intervals for a given thread with a resolution
//     *
//     * @param module
//     *            The kernel analysis instance to run this method on
//     * @param threadId
//     *            The ID of the thread to get the intervals for
//     * @param start
//     *            The start time of the requested range
//     * @param end
//     *            The end time of the requested range
//     * @param resolution
//     *            The resolution or the minimal time between the requested
//     *            intervals. If interval times are smaller than resolution, only
//     *            the first interval is returned, the others are ignored.
//     * @param monitor
//     *            A progress monitor for this task
//     * @return The list of status intervals for this thread, an empty list is
//     *         returned if either the state system is {@code null} or the quark
//     *         is not found
//     */
//    public static List<ITmfStateInterval> getStatusIntervalsForThread(KernelAnalysisModule module, Integer threadId, long start, long end, long resolution, IProgressMonitor monitor) {
//        ITmfStateSystem ss = module.getStateSystem();
//        if (ss == null) {
//            return Collections.EMPTY_LIST;
//        }
//
//        try {
//            int threadQuark = ss.getQuarkAbsolute(Attributes.THREADS, threadId.toString());
//            List<ITmfStateInterval> statusIntervals = StateSystemUtils.queryHistoryRange(ss, threadQuark,
//                    Math.max(start, ss.getStartTime()), Math.min(end - 1, ss.getCurrentEndTime()), resolution, null);
//            return statusIntervals;
//        } catch (AttributeNotFoundException | StateSystemDisposedException | TimeRangeException e) {
//        }
//        return Collections.EMPTY_LIST;
//    }

}
