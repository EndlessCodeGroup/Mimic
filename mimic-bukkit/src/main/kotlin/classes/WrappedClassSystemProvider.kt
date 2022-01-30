package ru.endlesscode.mimic.classes

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import ru.endlesscode.mimic.WrappedMimicService

internal class WrappedClassSystemProvider(
    override val delegate: BukkitClassSystem.Provider,
    pluginName: String,
    pluginManager: PluginManager,
) : WrappedMimicService(pluginName, pluginManager), BukkitClassSystem.Provider {

    constructor(delegate: BukkitClassSystem.Provider, plugin: Plugin)
            : this(delegate, plugin.name, plugin.server.pluginManager)

    override fun getSystem(player: Player): BukkitClassSystem = delegate.getSystem(player)
}
