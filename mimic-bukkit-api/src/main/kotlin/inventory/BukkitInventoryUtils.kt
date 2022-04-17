@file:JvmName("BukkitInventoryUtils")

package ru.endlesscode.mimic.inventory

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

/** Returns inventory's contents that is not `null` or AIR. */
public val Inventory.storedItems: List<ItemStack>
    get() = storageContents.asSequence()
            .filterNot { it.isNullOrEmpty() }
            .toList()

/** Returns list of player's equipped items. Can't contain `null` or AIR. */
public val PlayerInventory.equippedItems: List<ItemStack>
    get() = sequenceOf(itemInMainHand, itemInOffHand)
        .plus(armorContents)
        .filterNot { it.isNullOrEmpty() }
        .toList()

private fun ItemStack?.isNullOrEmpty(): Boolean = this == null || this.type.isAir
