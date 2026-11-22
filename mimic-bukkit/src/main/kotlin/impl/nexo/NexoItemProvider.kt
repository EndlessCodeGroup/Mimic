// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.nexo

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.internal.NormalizedItemProvider

/**
 * Item provider for Nexo items, see [NormalizedItemProvider] for item IDs.
 * Items don't depend on a player.
 */
internal class NexoItemProvider(
    private val nexo: NexoWrapper = NexoWrapper(),
) : NormalizedItemProvider("Nexo") {

    override val id: String = ID
    override val isEnabled: Boolean get() = nexo.isEnabled

    override val nativeItemIds: Collection<String> get() = nexo.itemIds

    override fun hasNativeItem(nativeId: String): Boolean = nexo.hasItem(nativeId)

    override fun getNativeItemId(item: ItemStack): String? = nexo.getItemId(item)

    override fun createNativeItem(nativeId: String, amount: Int, player: Player?): ItemStack? =
        nexo.createItem(nativeId, amount)

    companion object {
        const val ID: String = "nexo"
    }
}
