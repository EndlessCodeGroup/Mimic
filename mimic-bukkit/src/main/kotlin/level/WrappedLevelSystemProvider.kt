// Copyright (c) 2022 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.level

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.WrappedMimicService

internal class WrappedLevelSystemProvider(
    private val delegate: BukkitLevelSystem.Provider,
    pluginName: String,
) : WrappedMimicService(delegate, pluginName), BukkitLevelSystem.Provider {

    constructor(delegate: BukkitLevelSystem.Provider, plugin: Plugin) : this(delegate, plugin.name)

    override fun getSystem(player: Player): BukkitLevelSystem = delegate.getSystem(player)
}
