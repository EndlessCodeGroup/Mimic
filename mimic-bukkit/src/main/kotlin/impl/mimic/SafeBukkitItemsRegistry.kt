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
import ru.endlesscode.mimic.internal.Log

internal fun ItemsRegistryProvider.tryGetKnownIds(): Collection<String> {
    return try {
        registry.knownIds
    } catch (e: Exception) {
        logImplementationError(this, e)
        emptyList()
    }
}

internal fun ItemsRegistryProvider.tryIsSameItem(itemStack: ItemStack, itemId: String): Boolean {
    return try {
        registry.isSameItem(itemStack, itemId)
    } catch (e: Exception) {
        logImplementationError(this, e)
        false
    }
}

internal fun ItemsRegistryProvider.tryIsItemExists(itemId: String): Boolean {
    return try {
        registry.isItemExists(itemId)
    } catch (e: Exception) {
        logImplementationError(this, e)
        false
    }
}

internal fun ItemsRegistryProvider.tryGetItemId(itemStack: ItemStack): String? {
    return try {
        registry.getItemId(itemStack)
    } catch (e: Exception) {
        logImplementationError(this, e)
        null
    }
}

internal fun ItemsRegistryProvider.tryGetItem(itemId: String, payload: Any?, amount: Int): ItemStack? {
    return try {
        registry.getItem(itemId, payload, amount)
    } catch (e: Exception) {
        logImplementationError(this, e)
        null
    }
}

private fun logImplementationError(provider: ItemsRegistryProvider, throwable: Throwable) {
    Log.w(throwable,
        "Error in BukkitItemsRegistry with id '${provider.registry.id}' " +
                "(implemented via ${provider.plugin.name}). " +
                "Please, report it to ${provider.plugin.description.authors}.")
}
