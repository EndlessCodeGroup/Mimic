// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.resources

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.WrappedMimicService

internal class WrappedResourceProvider(
    private val delegate: ResourceProvider,
    plugin: Plugin,
) : WrappedMimicService(delegate, plugin.name), ResourceProvider {

    override val allResources: Collection<ResourceProvider.ResourceDefinition>
        get() = delegate.allResources

    override fun getPrimaryResource(player: Player): ResourceProvider.ResourceDefinition? =
        delegate.getPrimaryResource(player)

    override fun getState(player: Player, resourceId: String): ResourceState? = delegate.getState(player, resourceId)

    override fun setAmount(player: Player, resourceId: String, amount: Double): Boolean =
        delegate.setAmount(player, resourceId, amount)
}
