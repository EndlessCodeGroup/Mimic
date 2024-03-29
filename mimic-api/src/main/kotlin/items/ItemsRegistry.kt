/*
 * This file is part of Mimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
 *
 * Mimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Mimic.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.items

import ru.endlesscode.mimic.MimicService

/** Service for getting items by theirs ID. Also, can be used to match ID with item. */
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
