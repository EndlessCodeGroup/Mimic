// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.oraxen

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.internal.NormalizedItemProvider

/**
 * Item provider for Oraxen custom items, see [NormalizedItemProvider] for item IDs.
 * Items don't depend on a player.
 */
internal class OraxenItemProvider(
    private val oraxen: OraxenWrapper = OraxenWrapper(),
) : NormalizedItemProvider("Oraxen") {

    override val id: String = ID
    override val isEnabled: Boolean get() = oraxen.isEnabled

    override val nativeItemIds: Collection<String> get() = oraxen.itemIds

    override fun hasNativeItem(nativeId: String): Boolean = oraxen.hasItem(nativeId)

    override fun getNativeItemId(item: ItemStack): String? = oraxen.getItemId(item)

    override fun createNativeItem(nativeId: String, amount: Int, player: Player?): ItemStack? =
        oraxen.createItem(nativeId)?.also { it.amount = amount }

    companion object {
        const val ID: String = "oraxen"
    }
}
