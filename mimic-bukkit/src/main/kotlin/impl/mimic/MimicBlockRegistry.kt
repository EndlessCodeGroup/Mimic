// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mimic

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.blocks.BukkitBlockRegistry
import ru.endlesscode.mimic.blocks.DropsConsumer
import ru.endlesscode.mimic.impl.vanilla.MinecraftBlockRegistry
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.internal.MimicId
import ru.endlesscode.mimic.internal.authorsMention
import ru.endlesscode.mimic.internal.parseMimicId
import java.util.concurrent.ConcurrentHashMap

/**
 * Block registry combining all block registries, using their IDs as namespaces.
 *
 * A block belongs to the registry with the highest priority recognizing it,
 * so a custom block built on a vanilla block is not recognized as the vanilla block.
 *
 * Invalid IDs, passed in or returned by registries, are ignored.
 */
@ExperimentalMimicApi
public class MimicBlockRegistry internal constructor(
    private val registries: ServiceRegistry<BukkitBlockRegistry>,
) : BukkitBlockRegistry {

    override val id: String = ID

    override val knownIds: Collection<String>
        get() = providers.flatMap { provider ->
            provider.tryGetKnownIds().mapNotNull { provider.namespaced(it) }
        }

    internal val providers: List<BlockRegistryProvider>
        get() = registries.enabledEntries

    /** IDs of registries which returned invalid IDs, to warn about each registry once. */
    private val invalidRegistries = ConcurrentHashMap.newKeySet<String>()

    override fun isSameBlock(block: Block, blockId: String): Boolean {
        val id = parseMimicId(blockId) ?: return false
        val (owner, _) = identify(block) ?: return false
        return id.matches(owner.registry.id) && owner.tryIsSameBlock(block, id.path)
    }

    override fun isSameBlockItem(blockItem: ItemStack, blockId: String): Boolean {
        val id = parseMimicId(blockId) ?: return false
        val (owner, _) = identify(blockItem) ?: return false
        return id.matches(owner.registry.id) && owner.tryIsSameBlockItem(blockItem, id.path)
    }

    override fun isBlockExists(blockId: String): Boolean {
        val id = parseMimicId(blockId) ?: return false
        return getRegistries(id).any { it.tryIsBlockExists(id.path) }
    }

    override fun getBlockId(block: Block): String? = identify(block)?.second

    override fun getBlockIdFromItem(blockItem: ItemStack): String? = identify(blockItem)?.second

    override fun getBlockItem(blockId: String, amount: Int): ItemStack? {
        val id = parseMimicId(blockId) ?: return null
        val provider = getRegistries(id).find { it.tryIsBlockExists(id.path) } ?: return null
        return provider.tryGetBlockItem(id.path, amount)
    }

    override fun setBlock(block: Block, blockId: String, payload: Any?): Boolean {
        if (!block.isEmpty) return false
        val id = parseMimicId(blockId) ?: return false
        val provider = getRegistries(id).find { it.tryIsBlockExists(id.path) } ?: return false
        return provider.trySetBlock(block, id.path, payload)
    }

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer<ItemStack>): Boolean {
        val (owner, _) = identify(block) ?: return false
        return owner.tryBreakBlock(block, tool, dropsConsumer)
    }

    private fun identify(block: Block) = identify { it.tryGetBlockId(block) }

    private fun identify(blockItem: ItemStack) = identify { it.tryGetBlockIdFromItem(blockItem) }

    /**
     * Returns the registry with the highest priority recognizing a block paired with the namespaced block ID,
     * or `null` if no registry recognizes it.
     *
     * If a registry fails to check the block or returns an invalid ID, the vanilla registry is skipped,
     * because it recognizes any block, including vanilla blocks backing custom ones.
     */
    private inline fun identify(
        getBlockId: (BlockRegistryProvider) -> Result<String?>,
    ): Pair<BlockRegistryProvider, String>? {
        var failed = false
        for (provider in providers) {
            if (failed && provider.registry.id == MinecraftBlockRegistry.ID) continue
            val blockId = getBlockId(provider).getOrElse { failed = true; continue } ?: continue
            val namespacedId = provider.namespaced(blockId)
            if (namespacedId == null) {
                failed = true
                continue
            }
            return provider to namespacedId
        }
        return null
    }

    /** Returns registries which may contain the block with the given [id]. */
    private fun getRegistries(id: MimicId): Sequence<BlockRegistryProvider> =
        providers.asSequence().filter { id.matches(it.registry.id) }

    /** Returns the given [blockId] prefixed with the registry ID, or `null` if the result is invalid. */
    private fun BlockRegistryProvider.namespaced(blockId: String): String? {
        val namespacedId = "${registry.id}:$blockId"
        if (parseMimicId(namespacedId) != null) return namespacedId

        if (invalidRegistries.add(registry.id)) {
            Log.w(
                "Block registry '${registry.id}' returned invalid ID '$blockId', such blocks are ignored. " +
                    "IDs may contain only a-z, 0-9, '.', '_', '-' and '/'. " +
                    "Please, report it to ${plugin.authorsMention}."
            )
        }
        return null
    }

    public companion object {
        public const val ID: String = "mimic"
    }
}
