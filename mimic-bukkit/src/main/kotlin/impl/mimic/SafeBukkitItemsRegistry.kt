// Copyright (c) 2021 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mimic

import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.internal.authorsMention

internal fun ItemsRegistryProvider.tryGetKnownIds(): Collection<String> {
    return try {
        registry.knownIds
    } catch (e: Throwable) {
        logImplementationError(this, e)
        emptyList()
    }
}

internal fun ItemsRegistryProvider.tryIsSameItem(itemStack: ItemStack, itemId: String): Boolean {
    return try {
        registry.isSameItem(itemStack, itemId)
    } catch (e: Throwable) {
        logImplementationError(this, e)
        false
    }
}

internal fun ItemsRegistryProvider.tryIsItemExists(itemId: String): Boolean {
    return try {
        registry.isItemExists(itemId)
    } catch (e: Throwable) {
        logImplementationError(this, e)
        false
    }
}

internal fun ItemsRegistryProvider.tryGetItemId(itemStack: ItemStack): String? {
    return try {
        registry.getItemId(itemStack)
    } catch (e: Throwable) {
        logImplementationError(this, e)
        null
    }
}

internal fun ItemsRegistryProvider.tryGetItem(itemId: String, payload: Any?, amount: Int): ItemStack? {
    return try {
        registry.getItem(itemId, payload, amount)
    } catch (e: Throwable) {
        logImplementationError(this, e)
        null
    }
}

private fun logImplementationError(provider: ItemsRegistryProvider, throwable: Throwable) {
    Log.w(throwable,
        "Error in ItemsRegistry '${provider.registry.id}' " +
                "implemented via ${provider.plugin}. " +
                "Please, report it to ${provider.plugin.authorsMention}.")
}
