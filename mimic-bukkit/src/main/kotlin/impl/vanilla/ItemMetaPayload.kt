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

@file:UseSerializers(EnchantmentSerializer::class, ItemFlagsSerializer::class)

package ru.endlesscode.mimic.impl.vanilla

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.hocon.decodeFromConfig
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import ru.endlesscode.mimic.internal.DI
import ru.endlesscode.mimic.internal.EnchantmentSerializer
import ru.endlesscode.mimic.internal.ItemFlagsSerializer

/**
 * Payload to configure item's [ItemMeta][org.bukkit.inventory.meta.ItemMeta].
 * @see org.bukkit.inventory.meta.ItemMeta
 */
@Serializable
public data class ItemMetaPayload(
    val displayName: String? = null,
    val localizedName: String? = null,
    val lore: List<String>? = null,
    val isUnbreakable: Boolean = false,
    val damage: Int = 0,
    val customModelData: Int? = null,
    val enchants: Map<Enchantment, Int> = emptyMap(),
    val itemFlags: Set<ItemFlag> = emptySet(),
) {

    public companion object {

        /**
         * Tries to parse [ItemMetaPayload] from the given [input].
         * Returns `null` if parsing failed or [input] is empty.
         *
         * Input should be formatted in [HOCON](https://github.com/lightbend/config/blob/main/HOCON.md).
         * Unknown fields are ignored.
         */
        @JvmStatic
        public fun parse(input: String): ItemMetaPayload? {
            val hocon = DI.hocon
            return hocon.decodeFromConfig(ConfigFactory.parseString(input))
        }
    }
}
