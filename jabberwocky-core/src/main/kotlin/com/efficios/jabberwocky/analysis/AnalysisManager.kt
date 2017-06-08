/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.analysis

import com.efficios.jabberwocky.project.ITraceProject
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import java.util.*

object AnalysisManager {

    private val analyses: MutableSet<IAnalysis> = HashSet()

    private val analysisCache = CacheBuilder.newBuilder()
            .weakKeys()
            .softValues()
            .build(object : CacheLoader<ITraceProject<*, *>, Set<IAnalysis>>() {
                override fun load(trace: ITraceProject<*, *>): Set<IAnalysis> {
                    return analyses
                            .filter { it.appliesTo(trace) }
                            .toSet()
                }
            })

    /**
     * Get all the registered analyses that apply to the given trace project.

     * @param project The trace project to get the analyses for
     * @return The set of analyses that apply to this project. It can be
     * *         empty, but not null
     */
    fun getApplicableAnalyses(project: ITraceProject<*, *>): Set<IAnalysis> {
        return analysisCache.getUnchecked(project)
    }

    /**
     * Registers an analysis to the manager.

     * @param analysis Analysis to register
     */
    fun registerAnalysis(analysis: IAnalysis) {
        if (analyses.any { it.name == analysis.name}) {
//            logWarning(String.format("Ignoring external analysis with existing name \"%s\"", analysis.name)) //$NON-NLS-1$
            return
        }

        analyses.add(analysis)
        analysisCache.invalidateAll()
    }

    /**
     * Unregisters an analysis from the manager.

     * @param analysis Analysis to unregister
     */
    fun unregisterAnalysis(analysis: IAnalysis) {
        analyses.remove(analysis)
        analysisCache.invalidateAll()
    }
}