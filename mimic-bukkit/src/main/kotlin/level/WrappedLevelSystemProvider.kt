package ru.endlesscode.mimic.level

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import ru.endlesscode.mimic.WrappedMimicService

internal class WrappedLevelSystemProvider(
    private val delegate: BukkitLevelSystem.Provider,
    pluginName: String,
    pluginManager: PluginManager,
) : WrappedMimicService(delegate, pluginName, pluginManager), BukkitLevelSystem.Provider {

    constructor(delegate: BukkitLevelSystem.Provider, plugin: Plugin)
            : this(delegate, plugin.name, plugin.server.pluginManager)

    override fun getSystem(player: Player): BukkitLevelSystem = delegate.getSystem(player)
}
