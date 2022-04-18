package ru.endlesscode.mimic.inventory

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.WrappedMimicService

@ExperimentalMimicApi
internal class WrappedPlayerInventoryProvider(
    private val delegate: BukkitPlayerInventory.Provider,
    pluginName: String,
    pluginManager: PluginManager,
) : WrappedMimicService(delegate, pluginName, pluginManager), BukkitPlayerInventory.Provider {

    constructor(delegate: BukkitPlayerInventory.Provider, plugin: Plugin)
            : this(delegate, plugin.name, plugin.server.pluginManager)

    override fun getSystem(player: Player): BukkitPlayerInventory = delegate.getSystem(player)
}
