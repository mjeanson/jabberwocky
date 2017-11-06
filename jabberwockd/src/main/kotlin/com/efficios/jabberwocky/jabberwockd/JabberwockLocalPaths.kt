/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.jabberwockd

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Local file system paths that will be used by the daemon.
 *
 * TODO Define paths to use for Windows and macOS
 */
object JabberwockLocalPaths {

    private const val appDirSuffix = "jabberwockd"
    private const val tracesDirSuffix = "traces"
    private const val projectsDirSuffix = "projects"

    val homeDir: Path

    val dataDir: Path
    val configDir: Path
    val cacheDir: Path

    /** Subdirectory to store input traces. Should go under 'dataDir' */
    val tracesDir: Path
    /** Subdirectory to store Jabberwocky trace projects. Should go under 'dataDir' */
    val projectsDir: Path

    init {
        val homeDirStr = System.getProperty("user.home")
                ?: System.getenv("HOME")
                ?: throw IllegalArgumentException("Cannot find user home directory. Try defining \$HOME.")
        homeDir = Paths.get(homeDirStr)

        val dataDirStr = System.getenv("XDG_DATA_HOME")
        dataDir = if (dataDirStr == null) {
            homeDir.resolve(Paths.get(".local", "share", appDirSuffix))
        } else {
            Paths.get(dataDirStr, appDirSuffix)
        }

        val configDirStr = System.getenv("XDG_CONFIG_HOME")
        configDir = if (configDirStr == null) {
            homeDir.resolve(Paths.get(".config", appDirSuffix))
        } else {
            Paths.get(configDirStr, appDirSuffix)
        }

        val cacheDirStr = System.getenv("XDG_CACHE_HOME")
        cacheDir = if (cacheDirStr == null) {
            homeDir.resolve(Paths.get(".cache", appDirSuffix))
        } else {
            Paths.get(cacheDirStr, appDirSuffix)
        }

        tracesDir = dataDir.resolve(tracesDirSuffix)
        projectsDir = dataDir.resolve(projectsDirSuffix)
    }
}
