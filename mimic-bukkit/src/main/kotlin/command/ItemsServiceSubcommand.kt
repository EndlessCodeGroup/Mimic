/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2020 Osip Fatkullin
 * Copyright (C) 2020 EndlessCode Group and contributors
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

package ru.endlesscode.mimic.bukkit.command

import co.aikar.commands.AbstractCommandManager
import co.aikar.commands.MimicCommand
import co.aikar.commands.annotation.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.endlesscode.mimic.bukkit.BukkitItemsService

@CommandAlias("%command")
@CommandPermission("%perm")
@Subcommand("items|i")
internal class ItemsServiceSubcommand(private val itemsService: BukkitItemsService) : MimicCommand() {

    override fun afterRegister(manager: AbstractCommandManager) {
        manager.getCommandCompletions().registerAsyncCompletion("item") { itemsService.knownIds }
    }

    @Subcommand("info|i")
    @Description("Show information about items service")
    fun info(sender: CommandSender) {
        sender.send(
            "&3Items Service: &7${itemsService.id}",
            "&3Known IDs amount: &7${itemsService.knownIds.size}"
        )
    }

    @Subcommand("give|g")
    @Description("Give item to player")
    @CommandCompletion("@players @item")
    fun give(
        sender: CommandSender,
        @Flags("other") player: Player,
        item: String,
        @Default("1") amount: Int
    ) {
        val itemStack = itemsService.getItem(item, amount)
        if (itemStack != null) {
            player.inventory.addItem(itemStack)
            sender.send("&6Gave ${itemStack.amount} [$item] to ${player.name}.")
        } else {
            sender.send("&cUnknown item '$item'.")
        }
    }

    @Subcommand("compare|c")
    @Description("Compare item in hand corresponds and given item")
    @CommandCompletion("@item")
    fun compare(
        @Flags("itemheld") player: Player,
        @Single item: String
    ) {
        val isSame = itemsService.isSameItem(player.inventory.itemInMainHand, item)
        player.send("&6Item in hand and '$item' are%s same.".format(if (isSame) "" else "n't"))
    }

    @Subcommand("id|d")
    @Description("Prints ID of item in hand")
    fun id(
        @Flags("itemheld") player: Player
    ) {
        val id = itemsService.getItemId(player.inventory.itemInMainHand)
        player.send("&6Id of item in hand is '$id'")
    }

    @Subcommand("find|f")
    @Description("Check that item with given ID exists")
    @CommandCompletion("@item")
    fun find(
        sender: CommandSender,
        @Single item: String
    ) {
        val itemExists = itemsService.isItemExists(item)
        sender.send("&6Item with id '$item'%s exists".format(if (itemExists) "" else " isn't"))
    }
}
