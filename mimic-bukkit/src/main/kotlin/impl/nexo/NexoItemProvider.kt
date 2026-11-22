// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.nexo

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.internal.NormalizedIds
import ru.endlesscode.mimic.items.ItemProvider

/**
 * Item provider for Nexo items.
 * Nexo IDs are lowercased and characters not allowed in item IDs are replaced with `_`: `Ruby Sword` -> `ruby_sword`.
 * If several Nexo IDs become the same item ID, the items not used for this ID have no ID.
 * Takes no payload, items don't depend on a player.
 */
internal class NexoItemProvider(
    private val nexo: NexoWrapper = NexoWrapper(),
) : ItemProvider {

    private val ids = NormalizedIds("Nexo", nexo::hasItem, nexo::itemIds)

    override val id: String = ID
    override val isEnabled: Boolean get() = nexo.isEnabled

    override val allItems: Collection<String> get() = ids.all

    override fun hasItem(itemId: String): Boolean = ids.nativeOf(itemId) != null

    override fun getItemId(item: ItemStack): String? = nexo.getItemId(item)?.let(ids::normalizedOf)

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? {
        val nexoId = ids.nativeOf(itemId) ?: return null
        return nexo.createItem(nexoId, amount)
    }

    companion object {
        const val ID: String = "nexo"
    }
}
