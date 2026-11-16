// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.ecoitems

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.items.ItemProvider

/**
 * Item provider for EcoItems. Items of other eco plugins are not provided.
 * EcoItems IDs are used as is, because eco allows only `a-z`, `0-9` and `_` in them.
 * Takes no payload, items don't depend on a player.
 */
internal class EcoItemsItemProvider(
    private val ecoItems: EcoItemsWrapper = EcoItemsWrapper(),
) : ItemProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = ecoItems.isEnabled

    override val allItems: Collection<String> get() = ecoItems.itemIds

    override fun hasItem(itemId: String): Boolean = ecoItems.hasItem(itemId)

    override fun getItemId(item: ItemStack): String? = ecoItems.getItemId(item)

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? =
        ecoItems.createItem(itemId)?.also { it.amount = amount }

    companion object {
        const val ID: String = "ecoitems"
    }
}
