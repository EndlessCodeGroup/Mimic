// Copyright (c) 2020 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // ItemsRegistry API is deprecated but still supported

package ru.endlesscode.mimic.impl.vanilla

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.items.BukkitItemsRegistry

/**
 * Items service implementation using material name as itemId.
 *
 * Supported payloads in [getItem]:
 * - [String] with [item components](https://minecraft.wiki/w/Data_component_format) in the format of the `/give` command,
 *   for example `[damage=10,unbreakable={}]`.
 *   Components syntax depends on the server version.
 *   Invalid components are ignored.
 * - [String] with [ItemMetaPayload] in HOCON format, see [ItemMetaPayload.parse].
 *   Name and lore support MiniMessage formatting.
 * - [ItemMetaPayload] instance.
 */
@Deprecated("Use Items API instead, vanilla items have the `minecraft` namespace there too.")
public class MinecraftItemsRegistry : BukkitItemsRegistry {

    override val id: String = ID

    override val knownIds: List<String> by lazy {
        Material.entries.asSequence()
            .filter { it.isItem }
            .map { it.name.lowercase() }
            .toList()
    }

    override fun isItemExists(itemId: String): Boolean = getMaterial(itemId) != null

    override fun getItemId(item: ItemStack): String = item.type.name.lowercase()

    override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? {
        val material = getMaterial(itemId) ?: return null
        val realAmount = amount.coerceIn(1, material.maxStackSize)

        if (payload is String && payload.startsWith("[")) {
            return try {
                Bukkit.getItemFactory().createItemStack("${material.key.asString()}$payload")
                    .apply { this.amount = realAmount }
            } catch (e: IllegalArgumentException) {
                Log.w(e, "[${javaClass.simpleName}] Ignoring invalid payload for item $itemId:\n$payload")
                ItemStack(material, realAmount)
            }
        }

        val minecraftPayload = ItemMetaPayload.of(payload)
        if (payload != null && minecraftPayload == null)  {
            Log.w("[${javaClass.simpleName}] Ignoring unsupported payload for item $itemId:\n$payload")
        }

        return ItemStack(material, realAmount).apply {
            if (minecraftPayload != null) itemMeta = itemMeta?.applyPayload(minecraftPayload)
        }
    }

    private fun getMaterial(name: String): Material? {
        return Material.getMaterial(name.uppercase())
            ?.takeIf { it.isItem }
    }

    public companion object {
        public const val ID: String = "minecraft"
    }
}
