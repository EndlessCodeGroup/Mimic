// Copyright (c) 2017 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.level

const val EXP_IN_LEVEL = 10.0

class DummyExpLevelConverter: ExpLevelConverter {
    override fun getExpToReachLevel(level: Int): Double = level * EXP_IN_LEVEL
}
