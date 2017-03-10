/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial generation with CodePro tools
 *   Alexandre Montplaisir - Clean up, consolidate redundant tests
 *   Patrick Tasse - Remove getSubField
 *******************************************************************************/

package com.efficios.jabberwocky.ctf.trace.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.efficios.jabberwocky.ctf.trace.ExtractedCtfTestTrace;
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTrace;
import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTraceIterator;
import com.efficios.jabberwocky.trace.event.field.IntegerValue;

/**
 * The class <code>CtfTmfEventTest</code> contains tests for the class
 * <code>{@link CtfTmfEvent}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class CtfTmfEventTest {

    @ClassRule
    public static final ExtractedCtfTestTrace ETT = new ExtractedCtfTestTrace(CtfTestTrace.KERNEL);

    private static final String VALID_FIELD = "ret";

    /**
     * <pre>
     * babeltrace output :
     * [11:24:42.440133097] (+?.?????????) sys_socketcall: { cpu_id = 1 }, { call = 17, args = 0xB7555F30 }
     * [11:24:42.440137077] (+0.000003980) exit_syscall: { cpu_id = 1 }, { ret = 4132 }
     * </pre>
     */

    private CtfTraceEvent fixture;

    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp() {
        GenericCtfTrace trace = ETT.getTrace();
        try (GenericCtfTraceIterator iter = trace.getIterator();) {
            /* This test uses the second event of the trace */
            iter.next();
            fixture = iter.next();
        }
    }

    /**
     * Run the CTFEvent(EventDefinition,StreamInputReader) constructor test.
     */
    @Test
    public void testCTFEvent_read() {
        assertNotNull(fixture);
    }

    /**
     * Run the int getCPU() method test.
     */
    @Test
    public void testGetCPU() {
        int result = fixture.getCpu();
        assertEquals(1, result);
    }

    /**
     * Run the String getEventName() method test.
     */
    @Test
    public void testGetEventName() {
        String result = fixture.getEventName();
        assertEquals("exit_syscall", result);
    }

    /**
     * Run the ArrayList<String> getFieldNames() method test.
     */
    @Test
    public void testGetFieldNames() {
        Collection<String> result = fixture.getFieldNames();
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    /**
     * Run the Object getFieldValue(String) method test.
     */
    @Test
    public void testGetFieldValue() {
        IntegerValue result = fixture.getField(VALID_FIELD, IntegerValue.class);
        assertNotNull(result);
        assertNotNull(result.getValue());
    }

    /**
     * Run the long getTimestamp() method test.
     */
    @Test
    public void testGetTimestamp() {
        long result = fixture.getTimestamp();
        assertEquals(1332170682440137077L, result);
    }

    /**
     * Test the custom CTF attributes methods. The test trace doesn't have any,
     * so the list of attributes should be empty.
     */
    @Test
    public void testGetAttributes() {
        Map<String, String> attributes = fixture.getAttributes();
        assertNotNull(attributes);
        assertEquals(0, attributes.size());
    }

}
