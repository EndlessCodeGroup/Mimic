// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.itemsadder

import dev.lone.itemsadder.api.CustomBlock
import dev.lone.itemsadder.api.CustomStack
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

/**
 * Wraps ItemsAdder static API.
 * ItemsAdder loads its data after the server start, its registries are empty until then.
 */
internal class ItemsAdderWrapper {

    val isEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")

    val itemIds: Set<String> get() = CustomStack.getNamespacedIdsInRegistry()
    val blockIds: Set<String> get() = CustomBlock.getNamespacedIdsInRegistry()

    fun hasItem(id: String): Boolean = CustomStack.isInRegistry(id)
    fun hasBlock(id: String): Boolean = CustomBlock.isInRegistry(id)

    /** Returns a new instance of the item with the given [id]. */
    fun getCustomStack(id: String): CustomStack? = CustomStack.getInstance(id)
    fun getCustomStack(item: ItemStack): CustomStack? = CustomStack.byItemStack(item)

    /** Returns a new instance of the item placing the block with the given [id]. */
    fun getCustomBlock(id: String): CustomBlock? = CustomBlock.getInstance(id)
    fun getCustomBlock(item: ItemStack): CustomBlock? = CustomBlock.byItemStack(item)
    fun getCustomBlock(block: Block): CustomBlock? = CustomBlock.byAlreadyPlaced(block)

    fun placeBlock(id: String, location: Location): Boolean = CustomBlock.place(id, location) != null
}

private const val DEFAULT_NAMESPACE = "itemsadder"

/**
 * Converts ItemsAdder ID to Mimic ID: `itemsadder:ruby_ore` -> `ruby_ore`, `my_blocks:ruby_ore` -> `my_blocks/ruby_ore`.
 * The `itemsadder` namespace is kept if the ID has `/`, so the first path segment is always a namespace.
 */
internal fun String.toMimicId(): String =
    (if ('/' in this) this else removePrefix("$DEFAULT_NAMESPACE:")).replaceFirst(':', '/')

/** Converts Mimic ID to ItemsAdder ID: `ruby_ore` -> `itemsadder:ruby_ore`, `my_blocks/ruby_ore` -> `my_blocks:ruby_ore`. */
internal fun itemsAdderId(id: String): String =
    if ('/' in id) id.replaceFirst('/', ':') else "$DEFAULT_NAMESPACE:$id"
