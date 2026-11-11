@file:OptIn(ExperimentalMimicApi::class)

package ru.endlesscode.mimic.impl.mimic

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.blocks.DropsConsumer
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.internal.authorsMention

internal fun BlockRegistryProvider.tryGetKnownIds(): Collection<String> {
    return try {
        registry.knownIds
    } catch (e: Throwable) {
        logImplementationError(this, e)
        emptyList()
    }
}

internal fun BlockRegistryProvider.tryIsSameBlock(block: Block, blockId: String): Boolean {
    return try {
        registry.isSameBlock(block, blockId)
    } catch (e: Throwable) {
        logImplementationError(this, e)
        false
    }
}

internal fun BlockRegistryProvider.tryIsSameBlockItem(blockItem: ItemStack, blockId: String): Boolean {
    return try {
        registry.isSameBlockItem(blockItem, blockId)
    } catch (e: Throwable) {
        logImplementationError(this, e)
        false
    }
}

internal fun BlockRegistryProvider.tryIsBlockExists(blockId: String): Boolean {
    return try {
        registry.isBlockExists(blockId)
    } catch (e: Throwable) {
        logImplementationError(this, e)
        false
    }
}

/** Returns failure if the registry failed, so it can be distinguished from a block not belonging to the registry. */
internal fun BlockRegistryProvider.tryGetBlockId(block: Block): Result<String?> =
    runCatching { registry.getBlockId(block) }.onFailure { logImplementationError(this, it) }

/** Returns failure if the registry failed, so it can be distinguished from an item not belonging to the registry. */
internal fun BlockRegistryProvider.tryGetBlockIdFromItem(blockItem: ItemStack): Result<String?> =
    runCatching { registry.getBlockIdFromItem(blockItem) }.onFailure { logImplementationError(this, it) }

internal fun BlockRegistryProvider.tryGetBlockItem(blockId: String, amount: Int): ItemStack? {
    return try {
        registry.getBlockItem(blockId, amount)
    } catch (e: Throwable) {
        logImplementationError(this, e)
        null
    }
}

internal fun BlockRegistryProvider.trySetBlock(block: Block, blockId: String, payload: Any?): Boolean {
    return try {
        registry.setBlock(block, blockId, payload)
    } catch (e: Throwable) {
        logImplementationError(this, e)
        false
    }
}

internal fun BlockRegistryProvider.tryBreakBlock(
    block: Block,
    tool: ItemStack?,
    dropsConsumer: DropsConsumer<ItemStack>,
): Boolean {
    return try {
        registry.breakBlock(block, tool, dropsConsumer)
    } catch (e: Throwable) {
        logImplementationError(this, e)
        false
    }
}


private fun logImplementationError(provider: BlockRegistryProvider, throwable: Throwable) {
    Log.w(throwable,
        "Error in BlockRegistry '${provider.registry.id}' " +
                "implemented via ${provider.plugin}. " +
                "Please, report it to ${provider.plugin.authorsMention}.")
}
