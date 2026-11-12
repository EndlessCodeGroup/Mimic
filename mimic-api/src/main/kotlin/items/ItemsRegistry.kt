// Copyright (c) 2020 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.items

import ru.endlesscode.mimic.MimicService

/**
 * Service for getting items by their ID. Also, can be used to match ID with item.
 *
 * Mimic uses [resource locations](https://minecraft.wiki/w/Resource_location) to identify items,
 * where the namespace is the ID of an items registry: `[namespace]:[itemId]`, e.g. `minecraft:acacia_boat`.
 * IDs without namespace or with the `mimic:` namespace are searched over all registries.
 *
 * ### Implementation Notes
 *
 * Implementations receive and return IDs without the registry namespace:
 * lowercase paths of `a-z`, `0-9`, `.`, `_`, `-` and `/`.
 * Mapping them to the plugin's own IDs is up to the implementation: `itemsadder:ruby_sword` -> `itemsadder/ruby_sword`.
 * Invalid IDs are ignored.
 */
public interface ItemsRegistry<ItemStackT : Any> : MimicService {

    /** Returns all known item IDs. */
    public val knownIds: Collection<String>

    /** Returns `true` if given [item] represented with given [itemId]. */
    public fun isSameItem(item: ItemStackT, itemId: String): Boolean = getItemId(item) == itemId

    /** Returns `true` if item with given [itemId] exists. */
    public fun isItemExists(itemId: String): Boolean

    /** Returns ID representing given [item], or `null` if the ID not found in this registry. */
    public fun getItemId(item: ItemStackT): String?

    /** Returns item by given [itemId], or `null` if the ID not found in this registry. */
    public fun getItem(itemId: String): ItemStackT? = getItem(itemId, payload = null, amount = 1)

    /**
     * Returns item stack with specified [payload] by given [itemId], or `null` if the ID not found in this registry.
     *
     * If [payload] is not `null`, item will be configured using it.
     */
    public fun getItem(itemId: String, payload: Any?): ItemStackT? = getItem(itemId, payload, amount = 1)

    /**
     * Returns item stack with specified [amount] by given [itemId], or `null` if ID not found in this registry.
     *
     * If given [amount] is greater than maximum possible, will use maximum possible amount.
     * Amount shouldn't be less than `1`.
     */
    public fun getItem(itemId: String, amount: Int): ItemStackT? = getItem(itemId, payload = null, amount)

    /**
     * Returns item stack with specified [amount] and [payload] by given [itemId],
     * or `null` if ID not found in this registry.
     *
     * If given [amount] is greater than maximum possible, will use maximum possible amount.
     * Amount shouldn't be less than `1`.
     *
     * Given [payload] may be used to configure item.
     */
    public fun getItem(itemId: String, payload: Any?, amount: Int): ItemStackT?
}
