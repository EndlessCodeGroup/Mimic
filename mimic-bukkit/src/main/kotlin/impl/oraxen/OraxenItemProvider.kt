// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.oraxen

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.items.ItemProvider
import java.util.concurrent.ConcurrentHashMap

/**
 * Item provider for Oraxen custom items.
 * Oraxen IDs are lowercased, characters not allowed in item ID are replaced with `_`: `Ruby Sword` -> `ruby_sword`.
 * If two IDs become the same, only one of the items is available.
 * Takes no payload, items don't depend on a player.
 */
internal class OraxenItemProvider(
    private val oraxen: OraxenWrapper = OraxenWrapper(),
) : ItemProvider {

    private val skippedIds = ConcurrentHashMap.newKeySet<String>()

    override val id: String = ID
    override val isEnabled: Boolean get() = oraxen.isEnabled

    override val allItems: Collection<String>
        get() = itemsByMimicId().keys

    override fun hasItem(itemId: String): Boolean = findItem(itemId) != null

    override fun getItemId(item: ItemStack): String? = oraxen.getItemId(item)?.toMimicId()

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? {
        val oraxenId = findItem(itemId) ?: return null
        return oraxen.createItem(oraxenId)?.also { it.amount = amount }
    }

    private fun findItem(itemId: String): String? = if (oraxen.hasItem(itemId)) itemId else itemsByMimicId()[itemId]

    private fun itemsByMimicId(): Map<String, String> = mimicIdsMap(oraxen.itemIds, skippedIds, TAG)

    companion object {
        const val ID: String = "oraxen"
        private const val TAG = "OraxenItemProvider"
    }
}
