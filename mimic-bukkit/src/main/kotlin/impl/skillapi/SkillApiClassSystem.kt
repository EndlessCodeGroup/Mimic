// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.skillapi

import com.sucy.skill.api.player.PlayerData
import org.bukkit.entity.Player
import ru.endlesscode.mimic.classes.BukkitClassSystem

/** Implementation of ClassSystem that uses SkillAPI. */
public class SkillApiClassSystem internal constructor(
    player: Player,
    private val skillApi: SkillApiWrapper
) : BukkitClassSystem(player) {

    override val classes: List<String>
        get() = playerData.classes.map { it.data.name }

    override val primaryClass: String?
        get() = playerData.mainClass?.data?.name

    private val playerData: PlayerData
        get() = skillApi.getPlayerData(player)

    internal class Provider : BukkitClassSystem.Provider {
        private val skillApi = SkillApiWrapper()

        override val isEnabled: Boolean get() = skillApi.isLoaded
        override val id: String = ID

        override fun getSystem(player: Player): BukkitClassSystem = SkillApiClassSystem(player, skillApi)
    }

    public companion object {
        public const val ID: String = "skillapi"
    }
}
