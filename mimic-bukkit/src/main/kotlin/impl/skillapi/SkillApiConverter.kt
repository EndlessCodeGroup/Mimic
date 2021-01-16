/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2018 Osip Fatkullin
 * Copyright (C) 2018 EndlessCode Group and contributors
 *
 * BukkitMimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BukkitMimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BukkitMimic.  If not, see <http://www.gnu.org/licenses/>.
 */

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
