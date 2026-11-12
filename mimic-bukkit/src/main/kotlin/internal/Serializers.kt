// Copyright (c) 2021 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.internal

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Registry
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag

internal object EnchantmentSerializer : KSerializer<Enchantment> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "org.bukkit.enchantments.Enchantment",
        PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder): Enchantment {
        val value = decoder.decodeString()
        val key = namespacedKeyOf(value.replace(" ", "_").lowercase())
            ?: throw SerializationException(
                "$value is not a valid key for enchantment, only latin letters, digits and symbol _ are allowed"
            )
        return Registry.ENCHANTMENT[key]
            ?: throw SerializationException(
                "$value is not a valid key for enchantment, " +
                    "must be one of: [${Registry.ENCHANTMENT.joinToString { it.key.toString() }}]"
            )
    }

    override fun serialize(encoder: Encoder, value: Enchantment) = encoder.encodeString(value.key.toString())
}

internal object ItemFlagsSerializer : KSerializer<ItemFlag> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "org.bukkit.inventory.ItemFlag",
        PrimitiveKind.STRING,
    )

    override fun deserialize(decoder: Decoder): ItemFlag {
        val value = decoder.decodeString()
        return enumValueOrNull<ItemFlag>(value.uppercase())
            ?: throw SerializationException("$value is not a valid ItemFlag, must be one of ${ItemFlag.entries}")
    }

    override fun serialize(encoder: Encoder, value: ItemFlag) = encoder.encodeString(value.name)
}

internal object MiniMessageComponentSerializer : KSerializer<Component> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "net.kyori.adventure.text.Component",
        PrimitiveKind.STRING,
    )

    private val mm = MiniMessage.miniMessage()

    override fun deserialize(decoder: Decoder): Component {
        return mm.deserialize(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Component) {
        encoder.encodeString(mm.serialize(value))
    }
}
