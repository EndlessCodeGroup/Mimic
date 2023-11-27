/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
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

package ru.endlesscode.mimic.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.entity.Player
import ru.endlesscode.mimic.impl.mimic.MimicItemsRegistry
import ru.endlesscode.mimic.items.BukkitItemsRegistry

/**
 * Commands to deal with items registries
 * ```
 * /mimic items info
 * /mimic items give [target] [amount] [item] (payload)
 * /mimic items compare [item]
 * /mimic items id
 * /mimic items find [item]
 * ```
 */
internal fun CommandAPICommand.itemsSubcommand(itemsRegistry: BukkitItemsRegistry) = subcommand("items") {
    subcommand("info") {
        withShortDescription("Show information about items service")
        executes(infoExecutor(itemsRegistry))
    }

    subcommand("give") {
        withShortDescription("Give item to player")
        playerArgument(TARGET)
        integerArgument(AMOUNT, min = 1, max = 64) {
            replaceSuggestions(ArgumentSuggestions.strings("1", "64"))
        }
        itemArgument(itemsRegistry)
        executes(giveExecutor(itemsRegistry))
    }

    subcommand("compare") {
        withShortDescription("Compare item in hand corresponds and given item")
        itemArgument(itemsRegistry)
        playerExecutor { sender, args ->
            val item: String by args
            val isSame = itemsRegistry.isSameItem(sender.inventory.itemInMainHand, item)
            sender.send("&6Item in hand and '$item' %s same.".format(if (isSame) "are" else "aren't"))
        }
    }

    subcommand("id") {
        withShortDescription("Prints ID of item in hand")
        playerExecutor { sender, _ ->
            val id = itemsRegistry.getItemId(sender.inventory.itemInMainHand)
            sender.send("&6Id of item in hand is '$id'")
        }
    }

    subcommand("find") {
        withShortDescription("Check that item with given ID exists")
        itemArgument(itemsRegistry)
        anyExecutor { sender, args ->
            val item: String by args
            val itemExists = itemsRegistry.isItemExists(item)
            sender.send("&6Item with id '$item'%s exists".format(if (itemExists) "" else " isn't"))
        }
    }
}

// We can use only greedy string if we need to allow colons because it requires quoting in non-greedy strings.
// https://github.com/Mojang/brigadier/blob/cf754c4ef654160dca946889c11941634c5db3d5/src/main/java/com/mojang/brigadier/StringReader.java#L169
private fun CommandAPICommand.itemArgument(itemsRegistry: BukkitItemsRegistry) = greedyStringArgument(ITEM) {
    replaceSuggestions(ArgumentSuggestions.stringCollection { itemsRegistry.knownIds })
}

private fun infoExecutor(itemsRegistry: BukkitItemsRegistry) = CommandExecutor { sender, _ ->
    val registries = (itemsRegistry as? MimicItemsRegistry)?.providers
        .orEmpty()
        .map { it.provider }
        .map { "  &f${it.id}: &7${it.knownIds.size}" }

    sender.send(
        "&3Items Service: &7${itemsRegistry.id}",
        "&3Known IDs amount: &7${itemsRegistry.knownIds.size}"
    )
    sender.send(registries)
}

private fun giveExecutor(itemsRegistry: BukkitItemsRegistry) = CommandExecutor { sender, args ->
    val target: Player by args
    val amount = args.getOrDefaultUnchecked(AMOUNT, 1)
    // We can use only one greedy string at the end, so we read item and its payload from the same argument
    val itemParts = args.getRaw(ITEM).orEmpty().split(" ", limit = 2)
    val item = itemParts.first()
    val payload = itemParts.getOrNull(1)

    val itemStack = itemsRegistry.getItem(item, payload, amount)
    if (itemStack != null) {
        target.inventory.addItem(itemStack)
        sender.send("&6Gave ${itemStack.amount} [$item] to ${target.name}.")
    } else {
        sender.send("&cUnknown item '$item'.")
    }
}

private const val TARGET = "target"
private const val ITEM = "item"
private const val AMOUNT = "amount"
private const val PAYLOAD = "payload"
