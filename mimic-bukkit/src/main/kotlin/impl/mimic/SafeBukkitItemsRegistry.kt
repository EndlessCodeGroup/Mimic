// Copyright (c) 2021 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION") // ItemsRegistry API is deprecated but still supported

package ru.endlesscode.mimic.impl.mimic

import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.call

internal fun ItemsRegistryProvider.tryGetKnownIds(): Collection<String> =
    call(default = emptyList()) { knownIds }

internal fun ItemsRegistryProvider.tryIsSameItem(itemStack: ItemStack, itemId: String): Boolean =
    call(default = false) { isSameItem(itemStack, itemId) }

internal fun ItemsRegistryProvider.tryIsItemExists(itemId: String): Boolean =
    call(default = false) { isItemExists(itemId) }

internal fun ItemsRegistryProvider.tryGetItemId(itemStack: ItemStack): String? =
    call(default = null) { getItemId(itemStack) }

internal fun ItemsRegistryProvider.tryGetItem(itemId: String, payload: Any?, amount: Int): ItemStack? =
    call(default = null) { getItem(itemId, payload, amount) }
