// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mimic

import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.LookupResult
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.ServiceEntry
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.call
import ru.endlesscode.mimic.items.ItemProvider
import ru.endlesscode.mimic.items.Items

internal typealias ItemProviderEntry = ServiceEntry<ItemProvider>

/**
 * Items combining items of all item providers, using provider IDs as namespaces:
 * `sword/excalibur` -> `mmoitems:sword/excalibur`.
 *
 * An item belongs to the provider with the highest priority recognizing it,
 * so a custom item built on a vanilla item is not recognized as the vanilla item.
 */
internal class MimicItems(itemProviders: ServiceRegistry<ItemProvider>) : Items {

    val providers = NamespacedProviders(itemProviders, hasVanillaValues = true)

    override fun getAllItems(): List<Key> = providers.enabled.flatMap { it.getAllItems() }

    override fun hasItem(itemId: Key): Boolean = findProvider(itemId)?.hasItem(itemId.value()) == true

    override fun lookupItem(value: String): LookupResult = providers.lookup(value) { path -> hasItem(path) }

    override fun getItemId(item: ItemStack): Key? = owner(item)?.second

    override fun isItem(item: ItemStack, itemId: Key): Boolean = getItemId(item) == itemId

    override fun createItem(itemId: Key, amount: Int, player: Player?, payload: String?): ItemStack? =
        findProvider(itemId)?.createItem(itemId.value(), amount, player, payload)

    /** Returns the provider owning the [item] paired with the ID of the item, or `null` if no provider recognizes it. */
    fun owner(item: ItemStack): Pair<ItemProviderEntry, Key>? = providers.owner(ITEM) { getItemId(item) }

    private fun findProvider(itemId: Key): ItemProviderEntry? =
        providers.find(itemId, ITEM) { path -> hasItem(path) }

    // IDs are checked inside call, so a provider returning nulls from Java is skipped
    private fun ItemProviderEntry.getAllItems(): List<Key> =
        call(default = emptyList()) { validValues(allItems, ITEM) { it }.map { key(it) } }

    private companion object {
        const val ITEM = "item"
    }
}

/** Returns `true` if this provider has the item with the given [path]. */
internal fun ItemProviderEntry.hasItem(path: String): Boolean = call(default = false) { hasItem(path) }

/**
 * Creates the item with the given [path] via this provider, keeping the amount within `1` and the maximum stack size.
 * A blank [payload] is passed to the provider as `null`.
 * Warns once if the provider can't create an existing item with the given [payload].
 */
internal fun ItemProviderEntry.createItem(path: String, amount: Int, player: Player?, payload: String?): ItemStack? {
    val minAmount = amount.coerceAtLeast(1)
    val nonBlankPayload = payload?.takeIf { it.isNotBlank() }
    return call(default = null) {
        val item = createItem(path, minAmount, player, nonBlankPayload)
        if (item == null && nonBlankPayload != null && hasItem(path)) {
            val itemId = key(path).asString()
            warnings.warn("invalid-payload:$itemId:$nonBlankPayload") {
                "Item '$itemId' isn't created, because $apiName '${service.id}' can't use payload '$nonBlankPayload'."
            }
        }
        item?.also { it.amount = it.amount.coerceIn(1, it.maxStackSize) }
    }
}

/** IDs of all registered item providers, including bridged items registries, the values of `disabled-item-providers` config option. */
internal fun Mimic.getItemProviderIds(): Set<String> = (items as MimicItems).providers.allIds
