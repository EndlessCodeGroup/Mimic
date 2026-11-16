// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // ItemsRegistry API is deprecated but still supported

package ru.endlesscode.mimic.items

import org.bukkit.inventory.ItemStack

/** [ItemsRegistry] for Bukkit. */
@Deprecated("Use Items API instead: implement ItemProvider to provide items, use Items to get them.")
public interface BukkitItemsRegistry : ItemsRegistry<ItemStack>
