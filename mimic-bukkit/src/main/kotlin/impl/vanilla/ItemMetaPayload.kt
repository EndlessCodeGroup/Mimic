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

package ru.endlesscode.mimic.impl.vanilla

import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag

/**
 * Payload to configure item's [ItemMeta][org.bukkit.inventory.meta.ItemMeta].
 * @see org.bukkit.inventory.meta.ItemMeta
 */
public data class ItemMetaPayload(
    val displayName: String? = null,
    val localizedName: String? = null,
    val lore: List<String>? = null,
    val isUnbreakable: Boolean = false,
    val damage: Int = 0,
    val customModelData: Int? = null,
    val enchants: Map<Enchantment, Int> = emptyMap(),
    val itemFlags: List<ItemFlag> = emptyList(),
)