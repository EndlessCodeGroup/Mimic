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

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.internal.colorized
import ru.endlesscode.mimic.items.BukkitItemsRegistry

/**
 * Items service implementation using material name as itemId.
 * Supports [ItemMetaPayload] as a payload in [getItem].
 */
public class MinecraftItemsRegistry : BukkitItemsRegistry {

    override val id: String = ID

    override val knownIds: List<String> by lazy {
        Material.values().asSequence()
            .filter { it.isItem }
            .map { it.name.lowercase() }
            .toList()
    }

    override fun isItemExists(itemId: String): Boolean = getMaterial(itemId) != null

    override fun getItemId(item: ItemStack): String = item.type.name.lowercase()

    override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? {
        val material = getMaterial(itemId) ?: return null
        val realAmount = amount.coerceIn(1, material.maxStackSize)

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
        setDisplayName(payload.name?.colorized())
        lore = payload.lore?.colorized()

        // Apply damage and custom model data
        isUnbreakable = payload.isUnbreakable
        (this as? Damageable)?.damage = payload.damage
        setCustomModelData(payload.customModelData)

        // Apply enchants and item flags
        payload.enchantments.forEach { (enchant, level) -> addEnchant(enchant, level, true) }
        addItemFlags(*payload.flags.toTypedArray())

        return this
    }

    public companion object {
        public const val ID: String = "minecraft"
    }
}
