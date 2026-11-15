// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.fabled

import org.bukkit.entity.Player
import ru.endlesscode.mimic.resources.ResourceState
import ru.endlesscode.mimic.resources.SingleResourceProvider
import studio.magemonkey.fabled.api.enums.ManaCost
import studio.magemonkey.fabled.api.enums.ManaSource

/**
 * Fabled mana as the primary resource `fabled:mana`. All classes of the player share it,
 * a class only gives it another name, like "Rage". The provider is disabled if mana is disabled in Fabled.
 */
internal class FabledResourceProvider(
    private val fabled: FabledWrapper = FabledWrapper(),
) : SingleResourceProvider() {

    override val id: String = ID
    override val isEnabled: Boolean get() = fabled.isEnabled && fabled.isManaEnabled

    override fun getState(player: Player): ResourceState? {
        val data = fabled.getLoadedData(player) ?: return null
        return ResourceState.of(data.mana, data.maxMana)
    }

    override fun setAmount(player: Player, amount: Double): Boolean {
        val data = fabled.getLoadedData(player) ?: return false
        // Not setMana: it fires no events, so other plugins can't cancel or change it
        val change = amount - data.mana
        if (change > 0) data.giveMana(change, ManaSource.SPECIAL) else if (change < 0) data.useMana(-change, ManaCost.SPECIAL)
        return true
    }

    companion object {
        const val ID = "fabled"
    }
}
