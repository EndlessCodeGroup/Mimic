// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.internal

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.items.ItemProvider

/**
 * Item provider for plugins whose IDs are converted with [NormalizedIds].
 * Native IDs are lowercased and characters not allowed in item IDs are replaced with `_`: `Ruby Sword` -> `ruby_sword`.
 * If several native IDs become the same item ID, the items not used for this ID have no ID.
 * Takes no payload.
 *
 * @param tag Name of the plugin for warnings.
 */
internal abstract class NormalizedItemProvider(tag: String) : ItemProvider {

    private val ids = NormalizedIds(tag, ::hasNativeItem) { nativeItemIds }

    /** All native IDs of the plugin's items. */
    protected abstract val nativeItemIds: Collection<String>

    protected abstract fun hasNativeItem(nativeId: String): Boolean

    /** Returns native ID of the [item], or `null` if it isn't an item of the plugin. */
    protected abstract fun getNativeItemId(item: ItemStack): String?

    /** Creates the item with the given [nativeId], see [ItemProvider.createItem]. */
    protected abstract fun createNativeItem(nativeId: String, amount: Int, player: Player?): ItemStack?

    final override val allItems: Collection<String> get() = ids.all

    final override fun hasItem(itemId: String): Boolean = ids.nativeOf(itemId) != null

    final override fun getItemId(item: ItemStack): String? = getNativeItemId(item)?.let(ids::normalizedOf)

    final override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? {
        val nativeId = ids.nativeOf(itemId) ?: return null
        return createNativeItem(nativeId, amount, player)
    }
}
