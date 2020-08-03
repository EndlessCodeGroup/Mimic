/*
 * This file is part of MimicAPI.
 * Copyright (C) 2020 Osip Fatkullin
 * Copyright (C) 2020 EndlessCode Group and contributors
 *
 * MimicAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MimicAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MimicAPI.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic

/** Service for getting custom items by theirs ID. Also can be used to match id with item. */
interface ItemsService<ItemStackT : Any> : MimicService {

    /** Returns all known item IDs. */
    val knownIds: Collection<String>

    /** Returns `true` if given [item] represented with given [itemId]. */
    @JvmDefault
    fun isSameItem(item: ItemStackT, itemId: String): Boolean = getItemId(item) == itemId

    /** Returns `true` if item with given [itemId] exists. */
    fun isItemExists(itemId: String): Boolean

    /** Returns id that represents given [item], or `null` if the id not found. */
    fun getItemId(item: ItemStackT): String?

    /** Returns item by given [itemId], or `null` if id not found. */
    @JvmDefault
    fun getItem(itemId: String): ItemStackT? = getItem(itemId, amount = 1)

    /**
     * Returns specified item stack with specified [amount] by given [itemId], or `null` if id not found.
     *
     * If given [amount] is greater than maximum possible, will use maximum possible amount.
     * Amount shouldn't be less than `1`.
     */
    fun getItem(itemId: String, amount: Int): ItemStackT?
}
