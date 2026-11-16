// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // ItemMetaPayload is deprecated but still supported

package ru.endlesscode.mimic.impl.vanilla

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.internal.OnceWarnings
import ru.endlesscode.mimic.items.ItemProvider

/**
 * Item provider using material name as item ID. It recognizes any item, so it is asked last.
 *
 * Payload is [item components](https://minecraft.wiki/w/Data_component_format) in the format of the `/give` command,
 * for example `[damage=10,unbreakable={}]`. Components syntax depends on the server version.
 *
 * Deprecated [ItemMetaPayload] in HOCON format is still accepted with a warning.
 */
internal class MinecraftItemProvider : ItemProvider {

    override val id: String = ID

    private val warnings = OnceWarnings()

    override val allItems: Set<String> by lazy {
        Material.entries.asSequence()
            .filter { it.isObtainableItem }
            .mapTo(LinkedHashSet()) { it.name.lowercase() }
    }

    override fun getItemId(item: ItemStack): String? = if (item.isEmpty) null else item.type.name.lowercase()

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? {
        val material = Material.getMaterial(itemId.uppercase())?.takeIf { it.isObtainableItem } ?: return null
        if (payload == null) return ItemStack(material, amount)

        if (payload.startsWith("[")) {
            return try {
                Bukkit.getItemFactory().createItemStack("${material.key.asString()}$payload")
                    .apply { this.amount = amount }
            } catch (_: IllegalArgumentException) {
                null
            }
        }

        val metaPayload = ItemMetaPayload.parse(payload) ?: return null
        warnings.warn("hocon-payload:$payload") {
            "[$TAG] Payload in HOCON format is deprecated, use item components instead, " +
                "for example [damage=10,unbreakable={}]. Deprecated payload of item $itemId: $payload"
        }
        return ItemStack(material, amount).apply { itemMeta = itemMeta?.applyPayload(metaPayload) }
    }

    private val Material.isObtainableItem: Boolean
        get() = !isLegacy && !isAir && isItem

    companion object {
        const val ID: String = "minecraft"
        private const val TAG = "MinecraftItemProvider"
    }
}
