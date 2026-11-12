// Copyright (c) 2022 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.vanilla

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.inventory.BukkitPlayerInventory

@OptIn(ExperimentalMimicApi::class)
public class MinecraftPlayerInventory(player: Player) : BukkitPlayerInventory(player) {

    override val equippedItems: List<ItemStack>
        get() = collectEquippedItems()

    override val storedItems: List<ItemStack>
        get() = collectStoredItems()

    internal class Provider : BukkitPlayerInventory.Provider {
        override val id: String = ID
        override fun getSystem(player: Player): BukkitPlayerInventory = MinecraftPlayerInventory(player)
    }

    public companion object {
        public const val ID: String = "minecraft"
    }
}
