// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.executableitems

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.internal.NormalizedItemProvider

/**
 * Item provider for ExecutableItems, see [NormalizedItemProvider] for item IDs.
 * An item created for a player is built by ExecutableItems with this player as its owner.
 */
internal class ExecutableItemsItemProvider(
    private val executableItems: ExecutableItemsWrapper = ExecutableItemsWrapper(),
) : NormalizedItemProvider("ExecutableItems") {

    override val id: String = ID
    override val isEnabled: Boolean get() = executableItems.isEnabled

    override val nativeItemIds: Collection<String> get() = executableItems.itemIds

    override fun hasNativeItem(nativeId: String): Boolean = executableItems.hasItem(nativeId)

    override fun getNativeItemId(item: ItemStack): String? = executableItems.getItemId(item)

    override fun createNativeItem(nativeId: String, amount: Int, player: Player?): ItemStack? =
        executableItems.createItem(nativeId, amount, player)

    companion object {
        const val ID: String = "executableitems"
    }
}
