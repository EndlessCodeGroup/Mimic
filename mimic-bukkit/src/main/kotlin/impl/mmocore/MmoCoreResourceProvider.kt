// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mmocore

import io.lumine.mythic.lib.player.resource.ResourceUpdateReason
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.resources.ResourceProvider
import ru.endlesscode.mimic.resources.ResourceProvider.ResourceDefinition
import ru.endlesscode.mimic.resources.ResourceState

/**
 * MMOCore resources: the primary resource `mmocore:mana`, `mmocore:stamina` and `mmocore:stellium`.
 * MMOCore keeps them per class, so they change when the player switches the class.
 */
internal class MmoCoreResourceProvider(
    private val mmoCore: MmoCoreWrapper = MmoCoreWrapper(),
) : ResourceProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = mmoCore.isEnabled

    override val allResources: Collection<ResourceDefinition> = listOf(MANA, STAMINA, STELLIUM)

    override fun getPrimaryResource(player: Player): ResourceDefinition = MANA

    override fun getState(player: Player, resourceId: String): ResourceState? {
        val resource = findResource(resourceId) ?: return null
        val data = mmoCore.getLoadedData(player) ?: return null
        return ResourceState.of(resource.getCurrent(data), resource.getMax(data))
    }

    override fun setAmount(player: Player, resourceId: String, amount: Double): Boolean {
        val resource = findResource(resourceId) ?: return false
        val data = mmoCore.getLoadedData(player) ?: return false
        // Fires PlayerResourceUpdateEvent, so other plugins can cancel or change it
        resource.setCurrent(data, amount, ResourceUpdateReason.OTHER)
        return true
    }

    private fun findResource(resourceId: String): PlayerResource? = when (resourceId) {
        MANA.id -> PlayerResource.MANA
        STAMINA.id -> PlayerResource.STAMINA
        STELLIUM.id -> PlayerResource.STELLIUM
        else -> null
    }

    companion object {
        const val ID = "mmocore"

        private val MANA = ResourceDefinition("mana", Component.text("Mana"))
        private val STAMINA = ResourceDefinition("stamina", Component.text("Stamina"))
        private val STELLIUM = ResourceDefinition("stellium", Component.text("Stellium"))
    }
}
