// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mimic

import net.kyori.adventure.key.Key
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.LookupResult
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.ServiceEntry
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.blocks.BlockProvider
import ru.endlesscode.mimic.blocks.Blocks
import ru.endlesscode.mimic.blocks.DropsConsumer
import ru.endlesscode.mimic.call
import ru.endlesscode.mimic.internal.Log

internal typealias BlockProviderEntry = ServiceEntry<BlockProvider>

/**
 * Blocks combining blocks of all block providers, using provider IDs as namespaces:
 * `default/palm_log` -> `craftengine:default/palm_log`.
 *
 * A block belongs to the provider with the highest priority recognizing it,
 * so a custom block built on a vanilla block is not recognized as the vanilla block.
 */
internal class MimicBlocks(blockProviders: ServiceRegistry<BlockProvider>) : Blocks {

    val providers = NamespacedProviders(blockProviders, hasVanillaValues = true)

    override fun getAllBlocks(): List<Key> = providers.enabled.flatMap { it.getAllBlocks() }

    override fun hasBlock(blockId: Key): Boolean = findProvider(blockId)?.hasBlock(blockId.value()) == true

    override fun lookupBlock(value: String): LookupResult = providers.lookup(value) { path -> hasBlock(path) }

    override fun getBlockId(block: Block): Key? = providers.owner(BLOCK) { getBlockId(block) }?.second

    override fun isBlock(block: Block, blockId: Key): Boolean = getBlockId(block) == blockId

    override fun getBlockIdFromItem(item: ItemStack): Key? = providers.owner(BLOCK) { getBlockIdFromItem(item) }?.second

    override fun createBlockItem(blockId: Key, amount: Int): ItemStack? {
        val provider = findProvider(blockId) ?: return null
        val minAmount = amount.coerceAtLeast(1)
        return provider.call(default = null) {
            createBlockItem(blockId.value(), minAmount)?.also { it.amount = it.amount.coerceIn(1, it.maxStackSize) }
        }
    }

    override fun setBlock(block: Block, blockId: Key, state: String?): Boolean {
        if (!block.isEmpty) return false
        val provider = findProvider(blockId) ?: return false
        val nonBlankState = state?.takeIf { it.isNotBlank() }
        return provider.call(default = false) { setBlock(block, blockId.value(), nonBlankState) }
    }

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer): Boolean {
        val (owner, _) = providers.owner(BLOCK) { getBlockId(block) } ?: return false
        return owner.call(default = false) { breakBlock(block, tool, dropsConsumer.dropAllOnFailure()) }
    }

    private fun findProvider(blockId: Key): BlockProviderEntry? =
        providers.find(blockId, BLOCK) { path -> hasBlock(path) }

    private fun BlockProviderEntry.hasBlock(path: String): Boolean = call(default = false) { hasBlock(path) }

    // IDs are checked inside call, so a provider returning nulls from Java is skipped
    private fun BlockProviderEntry.getAllBlocks(): List<Key> =
        call(default = emptyList()) { validValues(allBlocks, BLOCK) { it }.map { key(it) } }

    private companion object {
        const val BLOCK = "block"
    }
}

/** Drops all items on the ground if the consumer fails, so they don't disappear when the block is already removed. */
private fun DropsConsumer.dropAllOnFailure() = DropsConsumer { drops ->
    try {
        consume(drops)
    } catch (cause: Throwable) {
        Log.w(cause, "Drops consumer failed, dropping all items on the ground")
        drops
    }
}

/** IDs of all registered block providers, the values of `disabled-block-providers` config option. */
internal fun Mimic.getBlockProviderIds(): Set<String> = (blocks as MimicBlocks).providers.allIds
