// Copyright (c) 2022 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // ItemsRegistry API is deprecated but still supported

package ru.endlesscode.mimic.items

import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.WrappedMimicService

internal class WrappedItemsRegistry(
    private val delegate: BukkitItemsRegistry,
    pluginName: String,
) : WrappedMimicService(delegate, pluginName), BukkitItemsRegistry {

    constructor(delegate: BukkitItemsRegistry, plugin: Plugin) : this(delegate, plugin.name)

    override val knownIds: Collection<String> by delegate::knownIds

    override fun isSameItem(item: ItemStack, itemId: String) = delegate.isSameItem(item, itemId)

    override fun isItemExists(itemId: String) = delegate.isItemExists(itemId)

    override fun getItemId(item: ItemStack) = delegate.getItemId(item)

    override fun getItem(itemId: String, payload: Any?, amount: Int) = delegate.getItem(itemId, payload, amount)
}
