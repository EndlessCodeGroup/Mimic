// Copyright (c) 2022 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.inventory

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.WrappedMimicService

@ExperimentalMimicApi
internal class WrappedPlayerInventoryProvider(
    private val delegate: BukkitPlayerInventory.Provider,
    pluginName: String,
) : WrappedMimicService(delegate, pluginName), BukkitPlayerInventory.Provider {

    constructor(delegate: BukkitPlayerInventory.Provider, plugin: Plugin) : this(delegate, plugin.name)

    override fun getSystem(player: Player): BukkitPlayerInventory = delegate.getSystem(player)
}
