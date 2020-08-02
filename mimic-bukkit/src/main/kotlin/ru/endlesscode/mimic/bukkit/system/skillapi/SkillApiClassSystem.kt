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
package ru.endlesscode.mimic.bukkit.system.skillapi

import com.sucy.skill.api.player.PlayerData
import org.bukkit.entity.Player
import ru.endlesscode.mimic.bukkit.system.BukkitClassSystem

/**
 * It's implementation of ClassSystem that uses SkillAPI.
 */
@registry.Subsystem(priority = registry.SubsystemPriority.NORMAL, classes = ["com.sucy.skill.SkillAPI"])
class SkillApiClassSystem internal constructor(
    player: Player,
    private val skillApi: SkillApiWrapper
) : BukkitClassSystem(player) {

    companion object {
        const val TAG = "SkillAPI"

        @JvmField
        val FACTORY = Factory(TAG, ::SkillApiClassSystem)
    }

    private constructor(player: Player) : this(player, SkillApiWrapper())

    override val name: String = TAG
    override val isEnabled: Boolean
        get() = skillApi.isLoaded

    override val classes: List<String>
        get() = playerData.classes.map { it.data.name }

    override val primaryClass: String?
        get() = playerData.mainClass?.data?.name

    private val playerData: PlayerData
        get() = skillApi.getPlayerData(player)
}
