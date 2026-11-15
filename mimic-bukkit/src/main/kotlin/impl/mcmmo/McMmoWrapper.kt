// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mcmmo

import com.gmail.nossr50.datatypes.player.McMMOPlayer
import com.gmail.nossr50.mcMMO
import com.gmail.nossr50.util.player.UserManager
import com.gmail.nossr50.util.skills.SkillTools
import org.bukkit.Bukkit
import org.bukkit.entity.Player

internal class McMmoWrapper {

    val isEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("mcMMO")

    val skillTools: SkillTools get() = mcMMO.p.skillTools

    /** mcMMO uses [Int.MAX_VALUE] for the unlimited power level. */
    val powerLevelCap: Int get() = mcMMO.p.generalConfig.powerLevelCap

    /** Returns `null` until mcMMO loads the player's profile. */
    fun getPlayer(player: Player): McMMOPlayer? = UserManager.getPlayer(player)
}
