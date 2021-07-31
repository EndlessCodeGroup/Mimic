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

package ru.endlesscode.mimic.internal

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment

internal object EnchantmentSerializer : KSerializer<Enchantment> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Enchantment", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Enchantment {
        val value = decoder.decodeString()
        val key = NamespacedKey.fromString(value.lowercase())
            ?: throw SerializationException("$value is not a valid namespaced key, " +
                    "only latin letters, digits and symbols /._- are allowed")
        return Enchantment.getByKey(key)
            ?: throw SerializationException("$value is not a valid key for enchantment, " +
                    "must be one of: [${Enchantment.values().joinToString { it.key.toString() }}]")
    }

    override fun serialize(encoder: Encoder, value: Enchantment) = encoder.encodeString(value.key.toString())
}
