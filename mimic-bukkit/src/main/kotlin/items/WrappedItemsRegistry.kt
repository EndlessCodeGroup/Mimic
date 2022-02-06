package ru.endlesscode.mimic.items

import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import ru.endlesscode.mimic.WrappedMimicService
import ru.endlesscode.mimic.config.MimicConfig

internal class WrappedItemsRegistry(
    private val delegate: BukkitItemsRegistry,
    private val config: MimicConfig,
    pluginName: String,
    pluginManager: PluginManager,
) : WrappedMimicService(delegate, pluginName, pluginManager), BukkitItemsRegistry {

    constructor(delegate: BukkitItemsRegistry, config: MimicConfig, plugin: Plugin)
            : this(delegate, config, plugin.name, plugin.server.pluginManager)

    override val isEnabled: Boolean
        get() = super<WrappedMimicService>.isEnabled && id !in config.disabledItemsRegistries

    override val knownIds: Collection<String> by delegate::knownIds

    override fun isItemExists(itemId: String) = delegate.isItemExists(itemId)

    override fun getItemId(item: ItemStack) = delegate.getItemId(item)

    override fun getItem(itemId: String, payload: Any?, amount: Int) = delegate.getItem(itemId, payload, amount)
}
