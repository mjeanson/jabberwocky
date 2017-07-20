/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

@file:JvmName("Javeltrace")
package com.efficios.jabberwocky.javeltrace;

import com.efficios.jabberwocky.ctf.trace.generic.GenericCtfTrace
import com.efficios.jabberwocky.project.TraceProject
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Simple standalone program taking a trace path in parameter and printing all
 * its events to stdout, one perline.
 *
 * Like Babeltrace, but in Java, hence the name.
 *
 * @author Alexandre Montplaisir
 */
fun main(args: Array<String>) {

    /* Parse the command-line parameters. */
    val tracePath = args[0]

    /* We're not keeping any state, so use a temporary directory. */
    val projectPath = Files.createTempDirectory("javeltrace-working-dir")

    /* Create the trace project */
    val trace = GenericCtfTrace(Paths.get(tracePath))
    val project = TraceProject.ofSingleTrace("MyProject", projectPath, trace)

    /* Retrieve an iterator on the project and read its events. */
    project.iterator().use {
        while (it.hasNext()) {
            val event = it.next()
            println(event.toString())
        }
    }

    /* Cleanup */
    projectPath.toFile().deleteRecursively()
}


