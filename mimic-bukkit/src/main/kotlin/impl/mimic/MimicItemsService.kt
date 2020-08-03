/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2020 Osip Fatkullin
 * Copyright (C) 2020 EndlessCode Group and contributors
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

package ru.endlesscode.mimic.bukkit.impl.mimic

import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.ServicesManager
import ru.endlesscode.mimic.bukkit.BukkitItemsService
import ru.endlesscode.mimic.bukkit.loadAll

/**
 * Items service combining all items services.
 * It uses service ID as namespace for items IDs.
 * For example: `acacia_boat` -> `minecraft:acacia_boat`
 */
class MimicItemsService(private val servicesManager: ServicesManager) : BukkitItemsService {

    override val isEnabled: Boolean = true
    override val id: String = "mimic"

    override val knownIds: Collection<String>
        get() = services.flatMap { service ->
            service.knownIds.map { service.namespaced(it) }
        }

    private val services: Collection<BukkitItemsService>
        get() = servicesManager.loadAll<BukkitItemsService>()
            .filterNot { it == this }

    override fun isSameItem(item: ItemStack, itemId: String): Boolean {
        return runOnServices(itemId) { id ->
            any { service -> service.isSameItem(item, id) }
        }
    }

    override fun isItemExists(itemId: String): Boolean {
        return runOnServices(itemId) { id ->
            any { service -> service.isItemExists(id) }
        }
    }

    override fun getItemId(item: ItemStack): String? {
        return services.asSequence()
            .map { service -> service.getItemId(item)?.let { service.namespaced(it) } }
            .filterNotNull()
            .firstOrNull()
    }

    override fun getItem(itemId: String, amount: Int): ItemStack? {
        return runOnServices(itemId) { id ->
            map { service -> service.getItem(id, amount) }
                .filterNotNull()
                .firstOrNull()
        }
    }

    private fun <T> runOnServices(itemId: String, block: Sequence<BukkitItemsService>.(id: String) -> T): T {
        val (namespace, id) = if (':' in itemId) {
            itemId.split(":", limit = 2)
        } else {
            listOf("", itemId)
        }

        return services.asSequence()
            .filter { it.namespaceMatches(namespace) }
            .block(id)
    }

    private fun BukkitItemsService.namespaced(itemId: String) = "$id:$itemId"
    private fun BukkitItemsService.namespaceMatches(namespace: String) = namespace.isEmpty() || namespace == id
}
