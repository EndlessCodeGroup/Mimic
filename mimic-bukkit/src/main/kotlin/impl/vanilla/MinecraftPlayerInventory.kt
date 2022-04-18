/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2022 Osip Fatkullin
 * Copyright (C) 2022 EndlessCode Group and contributors
 *
 * BukkitMimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BukkitMimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BukkitMimic.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.endlesscode.mimic.impl.vanilla

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.inventory.BukkitPlayerInventory
import ru.endlesscode.mimic.inventory.equippedItems
import ru.endlesscode.mimic.inventory.storedItems

@OptIn(ExperimentalMimicApi::class)
public class MinecraftPlayerInventory(player: Player) : BukkitPlayerInventory(player) {

    private val inventory: PlayerInventory get() = player.inventory

    override val equippedItems: List<ItemStack>
        get() = inventory.equippedItems

    override val storedItems: List<ItemStack>
        get() = inventory.storedItems

    internal class Provider : BukkitPlayerInventory.Provider {
        override val id: String = ID
        override fun getSystem(player: Player): BukkitPlayerInventory = MinecraftPlayerInventory(player)
    }

    public companion object {
        public const val ID: String = "minecraft"
    }
}
