// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.itemsadder

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.internal.NormalizedItemProvider

/**
 * Item provider for ItemsAdder custom items.
 * The default `itemsadder` namespace is dropped from item ID, other namespaces become the first path segment:
 * `itemsadder:ruby_sword` -> `ruby_sword`, `my_items:ruby_sword` -> `my_items/ruby_sword`.
 * Then the ID is converted as described in [NormalizedItemProvider]: `My_Items:Ruby_Sword` -> `my_items/ruby_sword`.
 * ItemsAdder can't create an item for a player, so the player is ignored.
 */
internal class ItemsAdderItemProvider(
    private val itemsAdder: ItemsAdderWrapper = ItemsAdderWrapper(),
) : NormalizedItemProvider("ItemsAdder") {

    override val id: String = ID
    override val isEnabled: Boolean get() = itemsAdder.isEnabled

    override val nativeItemIds: Collection<String> get() = itemsAdder.itemIds.map(String::toMimicId)

    override fun hasNativeItem(nativeId: String): Boolean = itemsAdder.hasItem(itemsAdderId(nativeId))

    override fun getNativeItemId(item: ItemStack): String? = itemsAdder.getCustomStack(item)?.namespacedID?.toMimicId()

    override fun createNativeItem(nativeId: String, amount: Int, player: Player?): ItemStack? =
        itemsAdder.getCustomStack(itemsAdderId(nativeId))?.itemStack?.also { it.amount = amount }

    companion object {
        const val ID: String = "itemsadder"

        /** ID of the items registry provided by ItemsAdder itself. */
        const val REGISTRY_ID: String = "ia"
    }
}
