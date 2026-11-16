// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.customitems

import com.jojodmo.customitems.api.CustomItemsAPI
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.items.ItemProvider

/**
 * Item provider for CustomItems. Takes no payload, items don't depend on a player.
 *
 * CustomItems can't list its items, so [allItems] has nothing to iterate over, but still checks that an item exists.
 */
internal class CustomItemsItemProvider : ItemProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = CustomItemsAPI.isEnabled()

    override val allItems: Set<String> = object : AbstractSet<String>() {
        override val size: Int = 0
        override fun iterator(): Iterator<String> = emptyList<String>().iterator()
        override fun contains(element: String): Boolean = CustomItemsAPI.getCustomItem(element) != null
    }

    override fun getItemId(item: ItemStack): String? = CustomItemsAPI.getCustomItemID(item)?.lowercase()

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? =
        CustomItemsAPI.getCustomItem(itemId, amount)

    companion object {
        const val ID: String = "customitems"
    }
}
