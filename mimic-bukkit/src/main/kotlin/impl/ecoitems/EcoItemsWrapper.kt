// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.ecoitems

import com.willfp.ecoitems.items.EcoItems
import com.willfp.ecoitems.items.ecoItem
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

/**
 * Wraps EcoItems own items registry.
 * It is used instead of eco's item lookup, because eco keeps items removed from EcoItems configs until restart.
 */
internal class EcoItemsWrapper {

    val isEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("EcoItems")

    val itemIds: List<String> get() = EcoItems.values().map { it.id.key }

    fun hasItem(id: String): Boolean = EcoItems.getByID(id) != null

    /** Returns ID of the [item], or `null` if the [item] isn't an EcoItems item. */
    fun getItemId(item: ItemStack): String? {
        if (item.isEmpty) return null
        return item.ecoItem?.id?.key
    }

    /** Returns a new stack of the item with the given [id], or `null` if there is no such item. */
    fun createItem(id: String): ItemStack? = EcoItems.getByID(id)?.itemStack
}
