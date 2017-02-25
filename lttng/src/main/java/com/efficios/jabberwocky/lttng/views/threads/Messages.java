/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.views.threads;

/**
 * Placeholder for package messages
 */
public class Messages {

//    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String ControlFlowRenderProvider_State_WaitUnknown = "WAIT_UNKNOWN";
    public static String ControlFlowRenderProvider_State_WaitBlocked = "WAIT_BLOCKED";
    public static String ControlFlowRenderProvider_State_WaitForCpu = "WAIT_FOR_CPU";
    public static String ControlFlowRenderProvider_State_UserMode = "USERMODE";
    public static String ControlFlowRenderProvider_State_Syscall = "SYSCALL";
    public static String ControlFlowRenderProvider_State_Interrupted = "INTERRUPTED";
    public static String ControlFlowRenderProvider_State_Unknown = "UNKNOWN";

    public static String ControlFlowSortingModes_ByTid = "Sort by TID";
    public static String ControlFlowSortingModes_ByThreadName = "Sort by Thread Name";

    public static String ControlFlowFilterModes_InactiveEntries = "Filter inactive entries";

//    static {
//        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
//    }

    private Messages() {
    }
}
