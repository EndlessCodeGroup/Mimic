// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.resources

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.resources.ResourceProvider.ResourceDefinition

/**
 * Provider of the single primary resource `mana`, for plugins with mana only.
 * Implement the methods without resource ID, requests for other resources return `null` or `false`.
 */
internal abstract class SingleResourceProvider : ResourceProvider {

    final override val allResources: Collection<ResourceDefinition> = listOf(MANA)

    final override fun getPrimaryResource(player: Player): ResourceDefinition = MANA

    final override fun getState(player: Player, resourceId: String): ResourceState? =
        if (resourceId == MANA.id) getState(player) else null

    final override fun setAmount(player: Player, resourceId: String, amount: Double): Boolean =
        resourceId == MANA.id && setAmount(player, amount)

    abstract fun getState(player: Player): ResourceState?
    open fun setAmount(player: Player, amount: Double): Boolean = false

    private companion object {
        val MANA = ResourceDefinition("mana", Component.text("Mana"))
    }
}
