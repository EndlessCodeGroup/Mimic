package ru.endlesscode.mimic.inventory

import org.jetbrains.annotations.ApiStatus
import ru.endlesscode.mimic.ExperimentalMimicApi

/**
 * API to access player's inventory.
 * Use [items], [equippedItems] or [storedItems] to get items from inventory.
 */
@ExperimentalMimicApi
@ApiStatus.Experimental
public interface PlayerInventory<ItemStackT : Any> {

    /**
     * Returns list containing all items from the inventory.
     * Items may not be null.
     */
    public val items: List<ItemStackT>
        get() = equippedItems + storedItems

    /**
     * Returns list containing all equipped items.
     * Items may not be null.
     */
    public val equippedItems: List<ItemStackT>

    /**
     * Returns list containing all not equipped items.
     * Items may not be null.
     */
    public val storedItems: List<ItemStackT>
}
