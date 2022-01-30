package ru.endlesscode.mimic.items

import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import ru.endlesscode.mimic.WrappedMimicService

internal class WrappedItemsRegistry(
    override val delegate: BukkitItemsRegistry,
    pluginName: String,
    pluginManager: PluginManager,
) : WrappedMimicService(pluginName, pluginManager), BukkitItemsRegistry {

    constructor(delegate: BukkitItemsRegistry, plugin: Plugin)
            : this(delegate, plugin.name, plugin.server.pluginManager)

    override val knownIds: Collection<String> by delegate::knownIds

    override fun isItemExists(itemId: String) = delegate.isItemExists(itemId)

    override fun getItemId(item: ItemStack) = delegate.getItemId(item)

    override fun getItem(itemId: String, payload: Any?, amount: Int) = delegate.getItem(itemId, payload, amount)
}
