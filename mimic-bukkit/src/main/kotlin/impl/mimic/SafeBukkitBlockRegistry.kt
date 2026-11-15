// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:OptIn(ExperimentalMimicApi::class)

package ru.endlesscode.mimic.impl.mimic

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.blocks.DropsConsumer
import ru.endlesscode.mimic.call
import ru.endlesscode.mimic.logImplementationError

internal fun BlockRegistryProvider.tryGetKnownIds(): Collection<String> =
    call(default = emptyList()) { knownIds }

internal fun BlockRegistryProvider.tryIsSameBlock(block: Block, blockId: String): Boolean =
    call(default = false) { isSameBlock(block, blockId) }

internal fun BlockRegistryProvider.tryIsSameBlockItem(blockItem: ItemStack, blockId: String): Boolean =
    call(default = false) { isSameBlockItem(blockItem, blockId) }

internal fun BlockRegistryProvider.tryIsBlockExists(blockId: String): Boolean =
    call(default = false) { isBlockExists(blockId) }

/** Returns failure if the registry failed, so it can be distinguished from a block not belonging to the registry. */
internal fun BlockRegistryProvider.tryGetBlockId(block: Block): Result<String?> =
    runCatching { registry.getBlockId(block) }.onFailure(::logImplementationError)

/** Returns failure if the registry failed, so it can be distinguished from an item not belonging to the registry. */
internal fun BlockRegistryProvider.tryGetBlockIdFromItem(blockItem: ItemStack): Result<String?> =
    runCatching { registry.getBlockIdFromItem(blockItem) }.onFailure(::logImplementationError)

internal fun BlockRegistryProvider.tryGetBlockItem(blockId: String, amount: Int): ItemStack? =
    call(default = null) { getBlockItem(blockId, amount) }

internal fun BlockRegistryProvider.trySetBlock(block: Block, blockId: String, payload: Any?): Boolean =
    call(default = false) { setBlock(block, blockId, payload) }

internal fun BlockRegistryProvider.tryBreakBlock(
    block: Block,
    tool: ItemStack?,
    dropsConsumer: DropsConsumer<ItemStack>,
): Boolean = call(default = false) { breakBlock(block, tool, dropsConsumer) }
