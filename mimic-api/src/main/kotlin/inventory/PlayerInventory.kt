package ru.endlesscode.mimic.inventory

import ru.endlesscode.mimic.ExperimentalMimicApi

/**
 * API to access player's inventory.
 * Use [items], [equippedItems] or [storedItems] to get items from inventory.
 */
@ExperimentalMimicApi
public interface PlayerInventory<ItemStackT : Any> {

    /**
     * Returns list containing all items from the inventory.
     * Items may not be null.
     */
    @ExperimentalMimicApi
    public val items: List<ItemStackT>
        get() = equippedItems + storedItems

    /**
     * Returns list containing all equipped items.
     * Items may not be null.
     */
    @ExperimentalMimicApi
    public val equippedItems: List<ItemStackT>

    /**
     * Returns list containing all not equipped items.
     * Items may not be null.
     */
    @ExperimentalMimicApi
    public val storedItems: List<ItemStackT>
}
