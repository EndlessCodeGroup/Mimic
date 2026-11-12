// Copyright (c) 2017 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.test

import io.kotest.matchers.doubles.ToleranceMatcher
import io.kotest.matchers.doubles.plusOrMinus

const val EPSILON = 0.0000001

val Double.nearly: ToleranceMatcher
    get() = plusOrMinus(EPSILON)
