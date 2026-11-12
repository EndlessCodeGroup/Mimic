// Copyright (c) 2022 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.internal

import org.bukkit.inventory.ItemStack

internal fun Sequence<ItemStack?>.filterNotNullOrEmpty(): Sequence<ItemStack> {
    return filterNotNull()
        .filterNot(ItemStack::isEmpty)
}
