// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.itemsadder

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.items.ItemProvider

/**
 * Item provider for ItemsAdder custom items.
 * The default `itemsadder` namespace is dropped from item ID, other namespaces become the first path segment:
 * `itemsadder:ruby_sword` -> `ruby_sword`, `my_items:ruby_sword` -> `my_items/ruby_sword`.
 * Takes no payload. ItemsAdder can't create an item for a player, so the player is ignored.
 */
internal class ItemsAdderItemProvider(
    private val itemsAdder: ItemsAdderWrapper = ItemsAdderWrapper(),
) : ItemProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = itemsAdder.isEnabled

    override val allItems: Collection<String>
        get() = itemsAdder.itemIds.map(String::toMimicId)

    override fun hasItem(itemId: String): Boolean = itemsAdder.hasItem(itemsAdderId(itemId))

    override fun getItemId(item: ItemStack): String? = itemsAdder.getCustomStack(item)?.namespacedID?.toMimicId()

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? {
        val customStack = itemsAdder.getCustomStack(itemsAdderId(itemId)) ?: return null
        return customStack.itemStack.also { it.amount = amount }
    }

    companion object {
        const val ID: String = "itemsadder"

        /** ID of the items registry provided by ItemsAdder itself. */
        const val REGISTRY_ID: String = "ia"
    }
}
