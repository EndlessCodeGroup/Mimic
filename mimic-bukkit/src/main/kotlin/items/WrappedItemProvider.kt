// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.items

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.WrappedMimicService

internal class WrappedItemProvider(
    private val delegate: ItemProvider,
    plugin: Plugin,
) : WrappedMimicService(delegate, plugin.name), ItemProvider {

    override val allItems: Collection<String>
        get() = delegate.allItems

    override fun hasItem(itemId: String): Boolean = delegate.hasItem(itemId)

    override fun getItemId(item: ItemStack): String? = delegate.getItemId(item)

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? =
        delegate.createItem(itemId, amount, player, payload)
}
