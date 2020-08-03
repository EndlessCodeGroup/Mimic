/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2020 Osip Fatkullin
 * Copyright (C) 2020 EndlessCode Group and contributors
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

package ru.endlesscode.mimic.bukkit.impl.vanilla

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.bukkit.BukkitItemsRegistry
import java.util.*

/** Items service implementation using material name as itemId. */
class MinecraftItemsRegistry : BukkitItemsRegistry {

    override val isEnabled: Boolean = true
    override val id: String = "minecraft"

    override val knownIds by lazy {
        Material.values().asSequence()
            .filter { it.isItem }
            .map { it.name.toLowerCase(Locale.ENGLISH) }
            .toList()
    }

    override fun isItemExists(itemId: String): Boolean = getMaterial(itemId) != null

    override fun getItemId(item: ItemStack): String? = item.type.name.toLowerCase(Locale.ENGLISH)

    override fun getItem(itemId: String, amount: Int): ItemStack? {
        val material = getMaterial(itemId) ?: return null
        val realAmount = amount.coerceIn(1, material.maxStackSize)
        return ItemStack(material, realAmount)
    }

    private fun getMaterial(name: String): Material? {
        return try {
            Material.valueOf(name.toUpperCase(Locale.ENGLISH))
                .takeIf { it.isItem }
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
