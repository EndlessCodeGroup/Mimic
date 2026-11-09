package ru.endlesscode.mimic.blocks

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.WrappedMimicService
import ru.endlesscode.mimic.config.MimicConfig

@ExperimentalMimicApi
internal class WrappedBlockRegistry(
    internal val delegate: BukkitBlockRegistry,
    private val config: MimicConfig,
    pluginName: String,
    pluginManager: PluginManager,
) : WrappedMimicService(delegate, pluginName, pluginManager), BukkitBlockRegistry {

    constructor(delegate: BukkitBlockRegistry, config: MimicConfig, plugin: Plugin)
            : this(delegate, config, plugin.name, plugin.server.pluginManager)

    override val isEnabled: Boolean
        get() = super<WrappedMimicService>.isEnabled && id !in config.disabledBlockRegistries

    override val knownIds: Collection<String> by delegate::knownIds

    override fun isSameBlock(block: Block, blockId: String) = delegate.isSameBlock(block, blockId)

    override fun isSameBlockItem(blockItem: ItemStack, blockId: String) = delegate.isSameBlockItem(blockItem, blockId)

    override fun isBlockExists(blockId: String) = delegate.isBlockExists(blockId)

    override fun getBlockId(block: Block) = delegate.getBlockId(block)

    override fun getBlockIdFromItem(blockItem: ItemStack) = delegate.getBlockIdFromItem(blockItem)

    override fun setBlock(block: Block, blockId: String, payload: Any?) = delegate.setBlock(block, blockId, payload)

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer<ItemStack>) =
        delegate.breakBlock(block, tool, dropsConsumer)
}

@ExperimentalMimicApi
internal fun BukkitBlockRegistry.unwrap() = if (this is WrappedBlockRegistry) delegate else this
