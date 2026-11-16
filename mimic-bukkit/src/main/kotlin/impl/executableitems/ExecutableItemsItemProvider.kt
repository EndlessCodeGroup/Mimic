// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.executableitems

import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.items.ItemProvider
import java.util.concurrent.ConcurrentHashMap

/**
 * Item provider for ExecutableItems.
 * ExecutableItems IDs are lowercased and characters not allowed in item IDs are replaced with `_`:
 * `Free_Hammer` -> `free_hammer`.
 * If several ExecutableItems IDs become the same item ID, the one equal to the item ID is used, otherwise the first one.
 * Takes no payload. An item created for a player is built by ExecutableItems with this player as its owner.
 */
internal class ExecutableItemsItemProvider(
    private val executableItems: ExecutableItemsWrapper = ExecutableItemsWrapper(),
) : ItemProvider {

    private val reportedDuplicates = ConcurrentHashMap.newKeySet<String>()

    override val id: String = ID
    override val isEnabled: Boolean get() = executableItems.isEnabled

    override val allItems: Collection<String>
        get() {
            val idsByMimicId = executableItems.itemIds.groupBy { it.toMimicId() }
            for ((mimicId, sameIds) in idsByMimicId) {
                if (sameIds.size > 1 && reportedDuplicates.add(mimicId)) {
                    val usedId = if (mimicId in sameIds) mimicId else sameIds.first()
                    Log.w("[ExecutableItems] IDs $sameIds have the same Mimic ID '$mimicId', only '$usedId' is used.")
                }
            }
            return idsByMimicId.keys.toList()
        }

    override fun hasItem(itemId: String): Boolean = findExecutableItemsId(itemId) != null

    override fun getItemId(item: ItemStack): String? = executableItems.getItemId(item)?.toMimicId()

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? {
        val executableItemsId = findExecutableItemsId(itemId) ?: return null
        return executableItems.createItem(executableItemsId, amount, player)
    }

    /** Returns ExecutableItems ID for the given [itemId], or `null` if there is no such item. */
    private fun findExecutableItemsId(itemId: String): String? =
        itemId.takeIf(executableItems::hasItem) ?: executableItems.itemIds.find { it.toMimicId() == itemId }

    private fun String.toMimicId(): String = buildString {
        for (char in this@toMimicId.lowercase()) append(if (Key.allowedInValue(char)) char else '_')
    }

    companion object {
        const val ID: String = "executableitems"
    }
}
