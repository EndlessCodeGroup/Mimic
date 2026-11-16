// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.nexo

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.items.ItemProvider

/**
 * Item provider for Nexo items. Item IDs are the same as in Nexo.
 * Takes no payload, items don't depend on a player.
 */
internal class NexoItemProvider(
    private val nexo: NexoWrapper = NexoWrapper(),
) : ItemProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = nexo.isEnabled

    override val allItems: Collection<String> get() = nexo.itemIds

    override fun hasItem(itemId: String): Boolean = nexo.hasItem(itemId)

    override fun getItemId(item: ItemStack): String? = nexo.getItemId(item)

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? =
        nexo.createItem(itemId, amount)

    companion object {
        const val ID: String = "nexo"
    }
}
