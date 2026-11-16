// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mmoitems

import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.items.ItemProvider

/**
 * Item provider for MMOItems.
 * MMOItems IDs are unique only within an item type, so the type is the first path segment of item ID:
 * `EXCALIBUR` of the type `SWORD` -> `sword/excalibur`.
 * Takes no payload. An item created for a player is built by MMOItems for this player.
 */
internal class MmoItemsItemProvider(
    private val mmoItems: MmoItemsWrapper = MmoItemsWrapper(),
) : ItemProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = mmoItems.isEnabled

    override val allItems: Collection<String>
        get() = mmoItems.templates.map { it.mimicId }

    override fun getItemId(item: ItemStack): String? {
        val type = mmoItems.getTypeName(item) ?: return null
        val id = mmoItems.getId(item) ?: return null
        return mimicId(type, id)
    }

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? {
        val template = mmoItems.templates.find { it.mimicId == itemId } ?: return null
        return mmoItems.buildItem(template, player)?.also { it.amount = amount }
    }

    private val MMOItemTemplate.mimicId: String
        get() = mimicId(mmoItems.getTypeName(this), id)

    private fun mimicId(type: String, id: String): String = "${type.lowercase()}/${id.lowercase()}"

    companion object {
        const val ID: String = "mmoitems"
    }
}
