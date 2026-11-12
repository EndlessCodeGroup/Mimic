// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.battlelevels

import ru.endlesscode.mimic.level.ExpLevelConverter

/** Converter for BattleLevels level system. */
public class BattleLevelsConverter private constructor(
    private val battleLevelsApi: BattleLevelsApiWrapper
) : ExpLevelConverter {

    public companion object {
        private var internalInstance: BattleLevelsConverter? = null

        @JvmStatic
        public val instance: ExpLevelConverter
            get() = getInstance()

        internal fun getInstance(battleLevelsApi: BattleLevelsApiWrapper? = null): ExpLevelConverter {
            return internalInstance
                ?: BattleLevelsConverter(battleLevelsApi ?: BattleLevelsApiWrapper()).also { internalInstance = it }
        }
    }

    override fun getExpToReachLevel(level: Int): Double {
        return battleLevelsApi.getNeededFor(level) - battleLevelsApi.getNeededFor(level - 1)
    }
}
