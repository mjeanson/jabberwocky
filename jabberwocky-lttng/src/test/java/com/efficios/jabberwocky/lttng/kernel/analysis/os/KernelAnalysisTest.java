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

import ca.polymtl.dorsal.libdelorean.IStateSystemReader;
import com.efficios.jabberwocky.lttng.testutils.ExtractedGenericCtfTestTrace;
import com.efficios.jabberwocky.lttng.testutils.ExtractedLttngKernelTestTrace;
import com.efficios.jabberwocky.project.TraceProject;
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
import java.util.List;
import java.util.stream.Stream;

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
    public static final ExtractedLttngKernelTestTrace KERNEL_TRACE = new ExtractedLttngKernelTestTrace(CtfTestTrace.KERNEL);
    @ClassRule
    public static final ExtractedGenericCtfTestTrace NON_KERNEL_TRACE = new ExtractedGenericCtfTestTrace(CtfTestTrace.CYG_PROFILE);

    private static final String PROJECT_NAME = "test-proj";
    private static final KernelAnalysis ANALYSIS = KernelAnalysis.instance();

    private Path kernelProjectPath;
    private Path nonKernelProjectPath;
    private TraceProject kernelProject;
    private TraceProject nonKernelProject;


    /**
     * Set-up the test
     */
    @Before
    public void setUp() {
        try {
            /* Will produce two different paths even if the prefix is the same. */
            kernelProjectPath = Files.createTempDirectory(PROJECT_NAME);
            nonKernelProjectPath = Files.createTempDirectory(PROJECT_NAME);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        kernelProject = TraceProject.ofSingleTrace(PROJECT_NAME, kernelProjectPath,  KERNEL_TRACE.getTrace());
        nonKernelProject = TraceProject.ofSingleTrace(PROJECT_NAME, nonKernelProjectPath, NON_KERNEL_TRACE.getTrace());
    }

    /**
     * Dispose test objects
     */
    @After
    public void tearDown() {
        Stream.of(kernelProjectPath, nonKernelProjectPath).forEach(path -> {
            if (path != null) {
                try {
                    MoreFiles.deleteRecursively(path);
                } catch (IOException e) {
                    /* Ignore */
                }
            }
        });
    }

    @Test
    public void testAppliesTo() {
        assertTrue(ANALYSIS.appliesTo(kernelProject));
        assertFalse(ANALYSIS.appliesTo(nonKernelProject));
    }

    /**
     * Test the canExecute method on valid and invalid traces
     */
    @Test
    public void testCanExecute() {
        assertTrue(ANALYSIS.canExecute(kernelProject));
        assertFalse(ANALYSIS.canExecute(nonKernelProject));
    }

    /**
     * Test the LTTng kernel analysis execution
     */
    @Test
    public void testAnalysisExecution() {
        IStateSystemReader ss = ANALYSIS.execute(kernelProject, null, null);
        assertNotNull(ss);

        List<Integer> quarks = ss.getQuarks("*");
        assertFalse(quarks.isEmpty());
    }

}
