package ru.endlesscode.mimic

/**
 * System for getting custom items by theirs id. Also can be used to match id with item.
 * @see MimicSystem
 */
interface ItemsSystem<ItemStackT : Any> : MimicSystem {

    /** Returns `true` if given [item] represented with given [itemId] in the system. */
    fun isSameItem(item: ItemStackT, itemId: String): Boolean

    /** Returns `true` if item with given [itemId] exists in the item system. */
    fun isItemExists(itemId: String): Boolean

    /** Returns id that represents given [item] in the system, or `null` if the id not found. */
    fun getItemId(item: ItemStackT): String?

    /** Returns item by given [itemId], or `null` if id not found in the system. */
    fun getItem(itemId: String): ItemStackT?
}
