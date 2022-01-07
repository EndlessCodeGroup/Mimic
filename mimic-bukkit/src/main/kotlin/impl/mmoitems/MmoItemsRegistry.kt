/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
 *
 * BukkitMimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BukkitMimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BukkitMimic.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.impl.mmoitems

import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.items.BukkitItemsRegistry

public class MmoItemsRegistry private constructor(private val mmoItems: MmoItemsWrapper) : BukkitItemsRegistry {

    public companion object {
        public const val ID: String = "mmoitems"
    }

    internal constructor() : this(MmoItemsWrapper())

    override val isEnabled: Boolean
        get() = mmoItems.isEnabled

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
}
