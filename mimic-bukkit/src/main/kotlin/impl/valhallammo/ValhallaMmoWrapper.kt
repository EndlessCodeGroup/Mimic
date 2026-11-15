// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.valhallammo

import me.athlaeos.valhallammo.playerstats.profiles.Profile
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry
import me.athlaeos.valhallammo.skills.skills.Skill
import me.athlaeos.valhallammo.skills.skills.SkillRegistry
import me.athlaeos.valhallammo.skills.skills.implementations.ConfigurableSkill
import org.bukkit.Bukkit
import org.bukkit.entity.Player

internal class ValhallaMmoWrapper {

    val isEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("ValhallaMMO")

    val skills: Collection<Skill> get() = SkillRegistry.getAllSkillsByType().values

    /** ValhallaMMO returns blank profiles and loses changes until profiles of the [player] are loaded. */
    fun isLoaded(player: Player): Boolean = ProfileRegistry.isLoaded(player)

    /** Returns the stored profile of the [skill] with the level and experience. */
    fun getProfile(player: Player, skill: Skill): Profile =
        if (skill is ConfigurableSkill) {
            ProfileRegistry.getPersistentConfigurableProfile(player, skill.type)
        } else {
            ProfileRegistry.getPersistentProfile(player, skill.profileType)
        }

    /** Returns the level limit of the [player], it is calculated from perks, so it isn't in the stored profile. */
    fun getMaxAllowedLevel(player: Player, skill: Skill): Int {
        val profile = if (skill is ConfigurableSkill) {
            ProfileCache.getOrCacheConfigurable(player, skill.type)
        } else {
            ProfileCache.getOrCache(player, skill.profileType)
        }
        return profile.maxAllowedLevel
    }
}
