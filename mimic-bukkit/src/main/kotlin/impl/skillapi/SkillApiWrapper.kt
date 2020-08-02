package ru.endlesscode.mimic.bukkit.impl.skillapi

import com.sucy.skill.SkillAPI
import com.sucy.skill.api.player.PlayerData
import com.sucy.skill.data.Settings
import org.bukkit.entity.Player

internal class SkillApiWrapper {
    val isLoaded: Boolean get() = SkillAPI.isLoaded()
    val settings: Settings get() = SkillAPI.getSettings()

    fun getPlayerData(player: Player): PlayerData = SkillAPI.getPlayerData(player)
}
