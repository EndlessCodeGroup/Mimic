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

package ru.endlesscode.mimic.impl.quantumrpg

import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.items.BukkitItemsRegistry

public class QuantumRpgItemsRegistry private constructor(
    private val quantumRpg: QuantumRpgWrapper,
) : BukkitItemsRegistry {

    internal constructor() : this(QuantumRpgWrapper())

    override val isEnabled: Boolean get() = quantumRpg.isEnabled
    override val id: String get() = ID

    override val knownIds: Collection<String>
        get() = quantumRpg.itemsModules
            .map { module -> module.itemIds.asSequence().map { module.namespaced(it) } }
            .flatten()
            .toList()

    override fun isSameItem(item: ItemStack, itemId: String): Boolean {
        val realItemId = getItemId(item) ?: return false
        return itemId == realItemId || itemId == realItemId.split(SEPARATOR, limit = 2).last()
    }

    override fun isItemExists(itemId: String): Boolean {
        return runOnModules(itemId) { id ->
            any { it.getItemById(id) != null }
        }
    }

    override fun getItemId(item: ItemStack): String? {
        return quantumRpg.itemsModules
            .mapNotNull { module -> module.getItemId(item)?.let { module.namespaced(it) } }
            .firstOrNull()
    }

    override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? {
        return runOnModules(itemId) { id ->
            mapNotNull { module -> module.getItemById(id) }
                .map { it.create() }
                .firstOrNull()
        }?.also { it.amount = amount.coerceIn(0, it.maxStackSize) }
    }

    private fun <T> runOnModules(itemId: String, block: Sequence<DropModule>.(id: String) -> T): T {
        val (namespace, id) = if (SEPARATOR in itemId) {
            itemId.split(SEPARATOR, limit = 2)
        } else {
            listOf("", itemId)
        }

        return quantumRpg.itemsModules
            .filter { it.namespaceMatches(namespace) }
            .block(id)
    }

    private fun DropModule.namespaced(itemId: String) = "$id$SEPARATOR$itemId"
    private fun DropModule.namespaceMatches(namespace: String) = namespace.isEmpty() || namespace == id

    public companion object {
        public const val ID: String = "quantumrpg"

        private const val SEPARATOR = '/'
    }
}
