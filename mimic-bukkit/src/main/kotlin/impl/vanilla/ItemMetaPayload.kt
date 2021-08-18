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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.hocon.decodeFromConfig
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import ru.endlesscode.mimic.internal.DI
import ru.endlesscode.mimic.internal.EnchantmentSerializer
import ru.endlesscode.mimic.internal.ItemFlagsSerializer
import ru.endlesscode.mimic.internal.Log

/**
 * Payload to configure item's [ItemMeta][org.bukkit.inventory.meta.ItemMeta].
 *
 * @property name Item name. You can specify colors using symbol `&`.
 * @property lore Item lore. You can specify colors using symbol `&`.
 * @property isUnbreakable Is item unbreakable. Affects only items that have durability (like weapons or tools).
 * @property damage Damage to item durability. Affects only items that have durability (like weapons or tools).
 * @property customModelData A value used to override item model.
 * @property enchantments Item's enchantments. See [Enchantment].
 * @property flags Item flags used to hide information about item from lore. See [ItemFlag].
 * @see org.bukkit.inventory.meta.ItemMeta
 * @see ItemMetaPayload.parse
 */
@Serializable
public data class ItemMetaPayload(
    val name: String? = null,
    val lore: List<String>? = null,
    @SerialName("unbreakable")
    val isUnbreakable: Boolean = false,
    val damage: Int = 0,
    val customModelData: Int? = null,
    val enchantments: Map<Enchantment, Int> = emptyMap(),
    val flags: Set<ItemFlag> = emptySet(),
) {

    public companion object {

        /**
         * Returns [ItemMetaPayload] if it can be retrieved from the given [payload] value.
         *
         * Supported types are:
         * - [ItemMetaPayload] - returns itself
         * - [String] - tries to parse payload using [ItemMetaPayload.parse]
         */
        @JvmStatic
        public fun of(payload: Any?): ItemMetaPayload? = when (payload) {
            is ItemMetaPayload -> payload
            is String -> parse(payload)
            else -> null
        }

        /**
         * Tries to parse [ItemMetaPayload] from the given [input].
         * Returns `null` if parsing failed or [input] is empty.
         *
         * Input should be formatted in [HOCON](https://github.com/lightbend/config/blob/main/HOCON.md).
         * Unknown fields are ignored.
         * ```
         * {
         *   name = "&6Item Name"
         *   lore = [Line1, Line2]
         *   unbreakable = true
         *   damage = 42
         *   custom-model-data = 1
         *   flags = [hide_attributes, hide_dye]
         *   enchantments = {unbreaking: 3}
         * }
         * ```
         */
        @JvmStatic
        public fun parse(input: String): ItemMetaPayload? {
            return try {
                DI.hocon.decodeFromConfig(ConfigFactory.parseString(input))
            } catch (e: Exception) {
                Log.w(e)
                null
            }
        }
    }
}
