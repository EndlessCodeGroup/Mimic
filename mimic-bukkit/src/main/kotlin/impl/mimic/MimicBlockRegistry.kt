package ru.endlesscode.mimic.impl.mimic

import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.blocks.BukkitBlockRegistry
import ru.endlesscode.mimic.blocks.DropsConsumer
import ru.endlesscode.mimic.impl.vanilla.MinecraftBlockRegistry

/**
 * Block registry combining all block registries, using their IDs as namespaces.
 *
 * A block belongs to the registry with the highest priority recognizing it,
 * so a custom block built on a vanilla block is not recognized as the vanilla block.
 */
@ExperimentalMimicApi
public class MimicBlockRegistry internal constructor(
    private val registries: ServiceRegistry<BukkitBlockRegistry>,
    private val parseKey: (String) -> NamespacedKey?,
) : BukkitBlockRegistry {

    override val id: String = ID

    override val knownIds: Collection<String>
        get() = providers.flatMap { provider ->
            provider.tryGetKnownIds().map { provider.namespaced(it) }
        }

    internal val providers: List<BlockRegistryProvider>
        get() = registries.enabledEntries

    override fun isSameBlock(block: Block, blockId: String): Boolean {
        val key = parseKey(blockId) ?: return false
        val (owner, _) = identify(block) ?: return false
        return key.matches(owner) && owner.tryIsSameBlock(block, key.key)
    }

    override fun isSameBlockItem(blockItem: ItemStack, blockId: String): Boolean {
        val key = parseKey(blockId) ?: return false
        val (owner, _) = identify(blockItem) ?: return false
        return key.matches(owner) && owner.tryIsSameBlockItem(blockItem, key.key)
    }

    override fun isBlockExists(blockId: String): Boolean {
        val key = parseKey(blockId) ?: return false
        return getRegistries(key).any { it.tryIsBlockExists(key.key) }
    }

    override fun getBlockId(block: Block): String? =
        identify(block)?.let { (provider, id) -> provider.namespaced(id) }

    override fun getBlockIdFromItem(blockItem: ItemStack): String? =
        identify(blockItem)?.let { (provider, id) -> provider.namespaced(id) }

    override fun getBlockItem(blockId: String, amount: Int): ItemStack? {
        val key = parseKey(blockId) ?: return null
        val provider = getRegistries(key).find { it.tryIsBlockExists(key.key) } ?: return null
        return provider.tryGetBlockItem(key.key, amount)
    }

    override fun setBlock(block: Block, blockId: String, payload: Any?): Boolean {
        if (!block.isEmpty) return false
        val key = parseKey(blockId) ?: return false
        val provider = getRegistries(key).find { it.tryIsBlockExists(key.key) } ?: return false
        return provider.trySetBlock(block, key.key, payload)
    }

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer<ItemStack>): Boolean {
        val (owner, _) = identify(block) ?: return false
        return owner.tryBreakBlock(block, tool, dropsConsumer)
    }

    private fun identify(block: Block) = identify { it.tryGetBlockId(block) }

    private fun identify(blockItem: ItemStack) = identify { it.tryGetBlockIdFromItem(blockItem) }

    /**
     * Returns the registry with the highest priority recognizing a block paired with the block ID,
     * or `null` if no registry recognizes it.
     *
     * If a registry fails to check the block, the vanilla registry is skipped,
     * because it recognizes any block, including vanilla blocks backing custom ones.
     */
    private inline fun identify(
        getBlockId: (BlockRegistryProvider) -> Result<String?>,
    ): Pair<BlockRegistryProvider, String>? {
        var failed = false
        for (provider in providers) {
            if (failed && provider.registry.id == MinecraftBlockRegistry.ID) continue
            val blockId = getBlockId(provider).getOrElse { failed = true; continue } ?: continue
            return provider to blockId
        }
        return null
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
