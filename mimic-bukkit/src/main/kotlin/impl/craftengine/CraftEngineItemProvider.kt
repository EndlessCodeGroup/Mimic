// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.craftengine

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.items.ItemProvider

/**
 * Item provider for CraftEngine custom items.
 * CraftEngine namespace becomes the first path segment of item ID: `default:palm_log` -> `default/palm_log`.
 * Vanilla items modified by CraftEngine are left to the `minecraft` provider.
 * Takes no payload. An item created for a player is built by CraftEngine for this player.
 */
internal class CraftEngineItemProvider(
    private val craftEngine: CraftEngineWrapper = CraftEngineWrapper(),
) : ItemProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = craftEngine.isEnabled

    /** A view of the loaded items, so checking that an item exists doesn't convert all IDs. */
    override val allItems: Set<String> = object : AbstractSet<String>() {
        override val size: Int get() = craftEngine.loadedCustomItems.size

        override fun iterator(): Iterator<String> =
            craftEngine.loadedCustomItems.asSequence().map { it.id().toMimicId() }.iterator()

        override fun contains(element: String): Boolean =
            craftEngineKey(element)?.let(craftEngine::getCustomItem) != null
    }

    override fun getItemId(item: ItemStack): String? = craftEngine.getCustomItem(item)?.id()?.toMimicId()

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? {
        val key = craftEngineKey(itemId) ?: return null
        return craftEngine.createCustomItem(key, amount, player)
    }

    companion object {
        const val ID: String = "craftengine"
    }
}
