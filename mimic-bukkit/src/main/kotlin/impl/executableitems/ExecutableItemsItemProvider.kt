// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.executableitems

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.internal.NormalizedIds
import ru.endlesscode.mimic.items.ItemProvider

/**
 * Item provider for ExecutableItems.
 * ExecutableItems IDs are lowercased and characters not allowed in item IDs are replaced with `_`:
 * `Free_Hammer` -> `free_hammer`.
 * If several ExecutableItems IDs become the same item ID, the items not used for this ID have no ID.
 * Takes no payload. An item created for a player is built by ExecutableItems with this player as its owner.
 */
internal class ExecutableItemsItemProvider(
    private val executableItems: ExecutableItemsWrapper = ExecutableItemsWrapper(),
) : ItemProvider {

    private val ids = NormalizedIds("ExecutableItems", executableItems::hasItem, executableItems::itemIds)

    override val id: String = ID
    override val isEnabled: Boolean get() = executableItems.isEnabled

    override val allItems: Collection<String> get() = ids.all

    override fun hasItem(itemId: String): Boolean = ids.nativeOf(itemId) != null

    override fun getItemId(item: ItemStack): String? = executableItems.getItemId(item)?.let(ids::normalizedOf)

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? {
        val executableItemsId = ids.nativeOf(itemId) ?: return null
        return executableItems.createItem(executableItemsId, amount, player)
    }

    companion object {
        const val ID: String = "executableitems"
    }
}
