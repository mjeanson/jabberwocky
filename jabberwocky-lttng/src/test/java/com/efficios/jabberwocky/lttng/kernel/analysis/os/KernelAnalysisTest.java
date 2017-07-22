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

import ca.polymtl.dorsal.libdelorean.ITmfStateSystem;
import com.efficios.jabberwocky.collection.ITraceCollection;
import com.efficios.jabberwocky.collection.TraceCollection;
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTrace;
import com.efficios.jabberwocky.lttng.kernel.trace.LttngKernelTrace;
import com.efficios.jabberwocky.lttng.kernel.trace.layout.LttngEventLayout;
import com.efficios.jabberwocky.lttng.trace.ExtractedCtfTestTrace;
import com.efficios.jabberwocky.project.ITraceProject;
import com.efficios.jabberwocky.project.TraceProject;
import com.efficios.jabberwocky.trace.ITrace;
import com.google.common.io.MoreFiles;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test the {@link KernelAnalysis} class
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault({})
@SuppressWarnings("rawtypes")
public class KernelAnalysisTest {

    @ClassRule
    public static final ExtractedCtfTestTrace KERNEL_TRACE = new ExtractedCtfTestTrace(CtfTestTrace.KERNEL);
    @ClassRule
    public static final ExtractedCtfTestTrace NON_KERNEL_TRACE = new ExtractedCtfTestTrace(CtfTestTrace.CYG_PROFILE);

    private static final String PROJECT_NAME = "test-proj";
    private static final KernelAnalysis ANALYSIS = new KernelAnalysis(LttngEventLayout.getInstance());

    private Path fProjectPath;

    private ITraceProject fKernelProject;
    private ITraceProject fNonKernelProject;


    /**
     * Set-up the test
     */
    @Before
    public void setUp() {
        try {
            fProjectPath = Files.createTempDirectory(PROJECT_NAME);
        } catch (IOException e) {
            fail(e.getMessage());
        }


        LttngKernelTrace kTrace = new LttngKernelTrace(KERNEL_TRACE.getTrace().getTracePath());
        GenericCtfTrace ustTrace = new GenericCtfTrace(NON_KERNEL_TRACE.getTrace().getTracePath());
        fKernelProject = newProject(kTrace);
        fNonKernelProject = newProject(ustTrace);
    }

    private ITraceProject newProject(ITrace trace) {
        ITraceCollection coll = new TraceCollection(Collections.singleton(trace));
        return new TraceProject(PROJECT_NAME, fProjectPath, Collections.singleton(coll));
    }

    /**
     * Dispose test objects
     */
    @After
    public void tearDown() {
        if (fProjectPath != null) {
            try {
                MoreFiles.deleteRecursively(fProjectPath);
            } catch (IOException e) {
            }
        }

    }

    @Test
    public void testAppliesTo() {
        assertTrue(ANALYSIS.appliesTo(fKernelProject));
        assertFalse(ANALYSIS.appliesTo(fNonKernelProject));
    }

    /**
     * Test the canExecute method on valid and invalid traces
     */
    @Test
    public void testCanExecute() {
        assertTrue(ANALYSIS.canExecute(fKernelProject));
        assertFalse(ANALYSIS.canExecute(fNonKernelProject));
    }

    /**
     * Test the LTTng kernel analysis execution
     */
    @Test
    public void testAnalysisExecution() {
        ITmfStateSystem ss = ANALYSIS.execute(fKernelProject, null, null);
        assertNotNull(ss);

        List<Integer> quarks = ss.getQuarks("*");
        assertFalse(quarks.isEmpty());
    }

}
