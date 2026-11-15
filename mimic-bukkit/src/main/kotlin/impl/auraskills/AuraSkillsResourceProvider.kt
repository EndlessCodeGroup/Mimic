// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.auraskills

import dev.aurelium.auraskills.api.user.SkillsUser
import org.bukkit.entity.Player
import ru.endlesscode.mimic.resources.ResourceState
import ru.endlesscode.mimic.resources.SingleResourceProvider

/** AuraSkills mana as the primary resource `auraskills:mana`. */
internal class AuraSkillsResourceProvider(
    private val auraSkills: AuraSkillsWrapper = AuraSkillsWrapper(),
) : SingleResourceProvider() {

    override val id: String = ID
    override val isEnabled: Boolean get() = auraSkills.isEnabled

    override fun getState(player: Player): ResourceState? {
        val user = player.loadedUser() ?: return null
        return ResourceState.of(user.mana, user.maxMana)
    }

    override fun setAmount(player: Player, amount: Double): Boolean {
        val user = player.loadedUser() ?: return false
        // Not consumeMana: it sends the "not enough mana" message
        user.mana = amount
        return true
    }

    /** AuraSkills ignores writes to not loaded users. */
    private fun Player.loadedUser(): SkillsUser? = auraSkills.api.getUser(uniqueId).takeIf { it.isLoaded }

    companion object {
        const val ID = "auraskills"
    }
}
