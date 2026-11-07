package ru.endlesscode.mimic.impl.mimic

import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.ServicesManager
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.blocks.BukkitBlockRegistry
import ru.endlesscode.mimic.blocks.DropsConsumer
import ru.endlesscode.mimic.bukkit.getRegistrations

/**
 * Block registry combining all block registries, using their IDs as namespaces.
 *
 * A block belongs to the registry with the highest priority recognizing it,
 * so a custom block built on a vanilla block is not recognized as the vanilla block.
 */
@ExperimentalMimicApi
public class MimicBlockRegistry internal constructor(
    private val servicesManager: ServicesManager,
    private val parseKey: (String) -> NamespacedKey?,
) : BukkitBlockRegistry {

    override val id: String = ID

    override val knownIds: Collection<String>
        get() = providers.flatMap { provider ->
            provider.tryGetKnownIds().map { provider.namespaced(it) }
        }

    internal val providers: List<BlockRegistryProvider>
        get() = servicesManager.getRegistrations<BukkitBlockRegistry>()
            .filter { it.registry.id != id && it.registry.isEnabled }

    override fun isSameBlock(block: Block, blockId: String): Boolean {
        val key = parseKey(blockId) ?: return false
        val owner = providers.find { it.tryGetBlockId(block) != null } ?: return false
        return key.matches(owner) && owner.tryIsSameBlock(block, key.key)
    }

    override fun isSameBlockItem(blockItem: ItemStack, blockId: String): Boolean {
        val key = parseKey(blockId) ?: return false
        val owner = providers.find { it.tryGetBlockIdFromItem(blockItem) != null } ?: return false
        return key.matches(owner) && owner.tryIsSameBlockItem(blockItem, key.key)
    }

    override fun isBlockExists(blockId: String): Boolean {
        val key = parseKey(blockId) ?: return false
        return getRegistries(key).any { it.tryIsBlockExists(key.key) }
    }

    override fun getBlockId(block: Block): String? =
        providers.firstNotNullOfOrNull { provider ->
            provider.tryGetBlockId(block)?.let { provider.namespaced(it) }
        }

    override fun getBlockIdFromItem(blockItem: ItemStack): String? =
        providers.firstNotNullOfOrNull { provider ->
            provider.tryGetBlockIdFromItem(blockItem)?.let { provider.namespaced(it) }
        }

    override fun setBlock(block: Block, blockId: String, payload: Any?): Boolean {
        val key = parseKey(blockId) ?: return false
        val provider = getRegistries(key).find { it.tryIsBlockExists(key.key) } ?: return false
        return provider.trySetBlock(block, key.key, payload)
    }

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer<ItemStack>): Boolean {
        val owner = providers.find { it.tryGetBlockId(block) != null } ?: return false
        return owner.tryBreakBlock(block, tool, dropsConsumer)
    }

    /** Returns registries which may contain the block with the given [key]. */
    private fun getRegistries(key: NamespacedKey): Sequence<BlockRegistryProvider> =
        providers.asSequence().filter { key.matches(it) }

    private fun NamespacedKey.matches(provider: BlockRegistryProvider): Boolean =
        namespace == ID || namespace == provider.registry.id

    private fun BlockRegistryProvider.namespaced(blockId: String) = "${registry.id}:$blockId"

    public companion object {
        public const val ID: String = "mimic"
    }
}
