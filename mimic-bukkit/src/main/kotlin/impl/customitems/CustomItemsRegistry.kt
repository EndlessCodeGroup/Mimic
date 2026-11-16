// Copyright (c) 2020 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // ItemsRegistry API is deprecated but still supported

package ru.endlesscode.mimic.impl.customitems

import com.jojodmo.customitems.api.CustomItemsAPI
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.items.BukkitItemsRegistry

@Deprecated("Use Items API instead, CustomItems items have the same IDs there.")
public class CustomItemsRegistry : BukkitItemsRegistry {

    override val id: String = ID
    override val isEnabled: Boolean get() = CustomItemsAPI.isEnabled()

    // INFO: CustomItems doesn't support returning list of known ids.
    override val knownIds: Collection<String> = emptyList()

    override fun isSameItem(item: ItemStack, itemId: String): Boolean = CustomItemsAPI.isCustomItem(item, itemId)

    override fun isItemExists(itemId: String): Boolean = CustomItemsAPI.getCustomItem(itemId) != null

    override fun getItemId(item: ItemStack): String? = CustomItemsAPI.getCustomItemID(item)?.lowercase()

    override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? = CustomItemsAPI.getCustomItem(itemId, amount)

    public companion object {
        public const val ID: String = "customitems"
    }
}
