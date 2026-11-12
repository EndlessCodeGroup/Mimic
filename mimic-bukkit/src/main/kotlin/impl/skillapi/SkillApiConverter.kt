// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.skillapi

import com.sucy.skill.data.Settings
import ru.endlesscode.mimic.level.ExpLevelConverter

/** Converter for SkillAPI level system. */
public class SkillApiConverter internal constructor(skillApi: SkillApiWrapper) :
    ExpLevelConverter {

    public companion object {
        private var internalInstance: SkillApiConverter? = null

        @JvmStatic
        public val instance: ExpLevelConverter
            get() = getInstance()

        internal fun getInstance(skillApi: SkillApiWrapper? = null): ExpLevelConverter {
            return internalInstance
                ?: SkillApiConverter(skillApi ?: SkillApiWrapper()).also { internalInstance = it }
        }
    }

    private val settings: Settings = skillApi.settings

    override fun getExpToReachLevel(level: Int): Double {
        return getExpToReachNextLevel(level - 1)
    }

    override fun getExpToReachNextLevel(level: Int): Double {
        return if (level <= 0) -1.0 else settings.getRequiredExp(level).toDouble()
    }
}
