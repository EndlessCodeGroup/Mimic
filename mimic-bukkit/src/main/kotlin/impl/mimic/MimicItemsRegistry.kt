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

package ru.endlesscode.mimic.impl.mimic

import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.ServicesManager
import ru.endlesscode.mimic.bukkit.getRegistrations
import ru.endlesscode.mimic.items.BukkitItemsRegistry

/**
 * Items registry combining all items registries.
 *
 * It uses service ID as namespace for items IDs.
 * For example: `acacia_boat` -> `minecraft:acacia_boat`.
 * If you use item ID without namespace it will search over all registries.
 */
public class MimicItemsRegistry(private val servicesManager: ServicesManager) : BukkitItemsRegistry {

    override val id: String = ID

    override val knownIds: Collection<String>
        get() = providers.flatMap { provider ->
            provider.tryGetKnownIds().map { provider.namespaced(it) }
        }

    internal val providers: List<ItemsRegistryProvider>
        get() = servicesManager.getRegistrations<BukkitItemsRegistry>()
            .filterNot { it.registry == this }

    override fun isSameItem(item: ItemStack, itemId: String): Boolean {
        val (namespace, id) = itemId.splitBySeparator()
        return getRegistries(namespace)
            .any { provider -> provider.tryIsSameItem(item, id) }
    }

    override fun isItemExists(itemId: String): Boolean {
        val (namespace, id) = itemId.splitBySeparator()
        return getRegistries(namespace)
            .any { provider -> provider.tryIsItemExists(id) }
    }

    override fun getItemId(item: ItemStack): String? {
        return providers.asSequence()
            .mapNotNull { provider -> provider.tryGetItemId(item)?.let { provider.namespaced(it) } }
            .firstOrNull()
    }

    override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? {
        val (namespace, id) = itemId.splitBySeparator()
        return getRegistries(namespace)
            .mapNotNull { service -> service.tryGetItem(id, payload, amount) }
            .firstOrNull()
    }

    private fun getRegistries(namespace: String): Sequence<ItemsRegistryProvider> {
        return if (namespace.isEmpty()) {
            providers.asSequence()
        } else {
            providers.asSequence()
                .filter { it.registry.id.equals(namespace, ignoreCase = true) }
        }
    }

    private fun String.splitBySeparator(): Pair<String, String> {
        return if (':' in this) {
            val parts = split(":", limit = 2)
            return parts[0] to parts[1];
        } else {
            "" to this
        }
    }

    private fun ItemsRegistryProvider.namespaced(itemId: String) = "${registry.id}:$itemId"

    public companion object {
        public const val ID: String = "mimic"
    }
}
