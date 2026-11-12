// Copyright (c) 2020 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.vanilla

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
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

    private fun ItemMeta.applyPayload(payload: ItemMetaPayload): ItemMeta {
        // Apply text options
        displayName(payload.name)
        lore(payload.lore)

        // Apply damage and custom model data
        isUnbreakable = payload.isUnbreakable
        (this as? Damageable)?.damage = payload.damage
        if (payload.customModelData != null) setCustomModelData(payload.customModelData)

        // Apply enchants and item flags
        payload.enchantments.forEach { (enchant, level) -> addEnchant(enchant, level, true) }
        addItemFlags(*payload.flags.toTypedArray())

        return this
    }

    public companion object {
        public const val ID: String = "minecraft"
    }
}
