/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.xychart.model.provider.statesystem

import ca.polymtl.dorsal.libdelorean.IStateSystemReader
import com.efficios.jabberwocky.analysis.statesystem.StateSystemAnalysis
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProvider
import com.efficios.jabberwocky.views.xychart.model.render.XYChartSeries

abstract class StateSystemXYChartProvider(providerName: String,
                                          stateSystemAnalysis: StateSystemAnalysis) : XYChartModelProvider(providerName) {

    var stateSystem: IStateSystemReader? = null
        private set

    init {
        /*
         * Change listener which will take care of keeping the target state
         * system up to date.
         */
        traceProjectProperty().addListener { _, _, newProject ->
            stateSystem = if (newProject != null
                    && stateSystemAnalysis.appliesTo(newProject)
                    && stateSystemAnalysis.canExecute(newProject)) {
                // TODO Cache this?
                stateSystemAnalysis.execute(newProject, null, null)
            } else {
                null
            }
        }
    }

}
