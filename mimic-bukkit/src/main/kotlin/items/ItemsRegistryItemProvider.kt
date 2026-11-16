// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // ItemsRegistry API is deprecated but still supported

package ru.endlesscode.mimic.items

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Exposes the given [ItemsRegistry][BukkitItemsRegistry] as an item provider.
 *
 * Registries may return IDs containing `:`, like `itemsadder:ruby_sword`. Such IDs are exposed with `/` instead.
 * A registry may return a shared stack, so created items are cloned.
 * The player is ignored, the registry can't create an item for a player.
 */
internal class ItemsRegistryItemProvider(private val registry: BukkitItemsRegistry) : ItemProvider {

    override val id: String get() = registry.id
    override val isEnabled: Boolean get() = registry.isEnabled

    override val allItems: Collection<String>
        get() = registry.knownIds.map(::toPath)

    // A registry may be unable to list its items, so ask it instead of looking in allItems
    override fun hasItem(itemId: String): Boolean = registry.isItemExists(toRegistryId(itemId))

    override fun getItemId(item: ItemStack): String? = registry.getItemId(item)?.let(::toPath)

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? =
        getItem(itemId, payload, amount)

    /** Creates item with a payload of any type, as [ItemsRegistry] allows. */
    fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? =
        registry.getItem(toRegistryId(itemId), payload, amount)?.clone()

    /** Compares the [item] with the [itemId] in the way of the registry, for example, ignoring a part of the ID. */
    fun isSameItem(item: ItemStack, itemId: String): Boolean = registry.isSameItem(item, toRegistryId(itemId))

    private fun toPath(registryId: String): String = registryId.replace(':', '/')

    /** Returns ID of the registry for the [path], restoring `:` replaced by [toPath]. */
    private fun toRegistryId(path: String): String {
        if ('/' !in path || registry.isItemExists(path)) return path
        return registry.knownIds.find { toPath(it) == path } ?: path
    }
}
