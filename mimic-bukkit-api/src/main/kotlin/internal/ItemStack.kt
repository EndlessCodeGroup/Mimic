package ru.endlesscode.mimic.internal

import org.bukkit.inventory.ItemStack

internal fun Sequence<ItemStack?>.filterNotNullOrEmpty(): Sequence<ItemStack> {
    return filterNotNull()
        .filterNot(ItemStack::isEmpty)
}

private fun ItemStack.isEmpty(): Boolean = this.type.isAir
