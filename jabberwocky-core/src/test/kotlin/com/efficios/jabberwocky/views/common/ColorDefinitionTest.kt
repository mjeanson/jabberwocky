/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.common

import org.junit.Test

class ColorDefinitionTest {

    @Test
    fun testValid() {
        ColorDefinition(  0,   0  , 0)
        ColorDefinition(128, 128, 128)
        ColorDefinition(255, 255, 255)

        ColorDefinition(  0,   0,   0,   0)
        ColorDefinition(128, 128, 128, 128)
        ColorDefinition(255, 255, 255, 255)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalid1() {
        ColorDefinition(-1, 0, 0, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalid2() {
        ColorDefinition(0, 0, 500, 0)
    }
}