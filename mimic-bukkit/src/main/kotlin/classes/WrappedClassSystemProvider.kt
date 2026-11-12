// Copyright (c) 2022 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.classes

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.WrappedMimicService

internal class WrappedClassSystemProvider(
    private val delegate: BukkitClassSystem.Provider,
    pluginName: String,
) : WrappedMimicService(delegate, pluginName), BukkitClassSystem.Provider {

    constructor(delegate: BukkitClassSystem.Provider, plugin: Plugin) : this(delegate, plugin.name)

    override fun getSystem(player: Player): BukkitClassSystem = delegate.getSystem(player)
}
