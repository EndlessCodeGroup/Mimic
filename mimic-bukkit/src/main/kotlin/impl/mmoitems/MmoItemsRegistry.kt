// Copyright (c) 2021 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mmoitems

import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.items.BukkitItemsRegistry

public class MmoItemsRegistry private constructor(private val mmoItems: MmoItemsWrapper) : BukkitItemsRegistry {

    internal constructor() : this(MmoItemsWrapper())

    override val isEnabled: Boolean get() = mmoItems.isEnabled
    override val id: String = ID

    override val knownIds: Collection<String>
        get() = templates.map { it.id.lowercase() }

    private val templates: Collection<MMOItemTemplate>
        get() = mmoItems.templatesManager.collectTemplates()

    override fun isItemExists(itemId: String): Boolean = itemId in knownIds

    override fun getItemId(item: ItemStack): String? {
        val nbtItem = mmoItems.getNbtItem(item)
        return nbtItem.getString("MMOITEMS_ITEM_ID")?.lowercase()
    }

    override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? {
        val template = templates.find { it.id.equals(itemId, ignoreCase = true) } ?: return null

        return template.buildItemStack()
            ?.also { it.amount = amount.coerceIn(1, it.maxStackSize) }
    }

    private fun MMOItemTemplate.buildItemStack(): ItemStack? {
        return newBuilder(0, null)
            .build()
            .newBuilder()
            .build()
    }

    public companion object {
        public const val ID: String = "mmoitems"
    }
}
