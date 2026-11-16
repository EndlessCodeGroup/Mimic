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
 * CustomItems can't list its items, so [allItems] is empty, but [hasItem] still checks that an item exists.
 */
internal class CustomItemsItemProvider : ItemProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = CustomItemsAPI.isEnabled()

    override val allItems: Collection<String> = emptyList()

    override fun hasItem(itemId: String): Boolean = CustomItemsAPI.getCustomItem(itemId) != null

    override fun getItemId(item: ItemStack): String? = CustomItemsAPI.getCustomItemID(item)?.lowercase()

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? =
        CustomItemsAPI.getCustomItem(itemId, amount)

    companion object {
        const val ID: String = "customitems"
    }
}
