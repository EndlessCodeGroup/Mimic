// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.oraxen

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.internal.NormalizedIds
import ru.endlesscode.mimic.items.ItemProvider

/**
 * Item provider for Oraxen custom items.
 * Oraxen IDs are lowercased, characters not allowed in item ID are replaced with `_`: `Ruby Sword` -> `ruby_sword`.
 * If several Oraxen IDs become the same item ID, the items not used for this ID have no ID.
 * Takes no payload, items don't depend on a player.
 */
internal class OraxenItemProvider(
    private val oraxen: OraxenWrapper = OraxenWrapper(),
) : ItemProvider {

    private val ids = NormalizedIds("Oraxen", oraxen::hasItem, oraxen::itemIds)

    override val id: String = ID
    override val isEnabled: Boolean get() = oraxen.isEnabled

    override val allItems: Collection<String> get() = ids.all

    override fun hasItem(itemId: String): Boolean = ids.nativeOf(itemId) != null

    override fun getItemId(item: ItemStack): String? = oraxen.getItemId(item)?.let(ids::normalizedOf)

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? {
        val oraxenId = ids.nativeOf(itemId) ?: return null
        return oraxen.createItem(oraxenId)?.also { it.amount = amount }
    }

    companion object {
        const val ID: String = "oraxen"
    }
}
