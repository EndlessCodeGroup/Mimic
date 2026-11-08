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

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import ru.endlesscode.mimic.impl.mimic.MimicItemsRegistry
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.items.unwrap

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
internal fun CommandBuilder.itemsSubcommand(itemsRegistry: BukkitItemsRegistry) = literal("items") {
    // Show information about items service
    literal("info") {
        executesAny(infoExecutor(itemsRegistry))
    }

    // Give item to player
    literal("give") {
        argument(TARGET, ArgumentTypes.player()) {
            argument(AMOUNT, IntegerArgumentType.integer(1, 64)) {
                suggestsStrings { listOf("1", "64") }
                itemArgument(itemsRegistry) {
                    executesAny(giveExecutor(itemsRegistry))
                }
            }
        }
    }

    // Compare item in hand corresponds and given item
    literal("compare") {
        itemArgument(itemsRegistry) {
            executesPlayer { sender, context ->
                val item = StringArgumentType.getString(context, ITEM)
                val isSame = itemsRegistry.isSameItem(sender.inventory.itemInMainHand, item)
                sender.sendMessage(successText("Item in hand and '$item' %s same.".format(if (isSame) "are" else "aren't")))
            }
        }
    }

    // Prints ID of item in hand
    literal("id") {
        executesPlayer { sender, _ ->
            val id = itemsRegistry.getItemId(sender.inventory.itemInMainHand)
            sender.sendMessage(successText("Id of item in hand is '$id'"))
        }
    }

    // Check that item with given ID exists
    literal("find") {
        itemArgument(itemsRegistry) {
            executesAny { sender, context ->
                val item = StringArgumentType.getString(context, ITEM)
                val itemExists = itemsRegistry.isItemExists(item)
                sender.sendMessage(successText("Item with id '$item'%s exists".format(if (itemExists) "" else " isn't")))
            }
        }
    }
}

// We can use only greedy string if we need to allow colons because it requires quoting in non-greedy strings.
// https://github.com/Mojang/brigadier/blob/cf754c4ef654160dca946889c11941634c5db3d5/src/main/java/com/mojang/brigadier/StringReader.java#L169
private fun CommandBuilder.itemArgument(
    itemRegistry: BukkitItemsRegistry,
    configure: CommandBuilder.() -> Unit,
) = argument(ITEM, StringArgumentType.greedyString()) {
    suggestsStrings { itemRegistry.knownIds }
    configure()
}

private fun infoExecutor(itemRegistry: BukkitItemsRegistry): CommandAction<CommandSender> = { sender, _ ->
    val providers = (itemRegistry.unwrap() as? MimicItemsRegistry)?.providers.orEmpty().map { it.provider }

    val message = text {
        appendStats(
            "Item Registry" to itemRegistry.id,
            "Known IDs amount" to itemRegistry.knownIds.size.toString(),
        )

        for (provider in providers) {
            append("  ${provider.id}: ", NamedTextColor.WHITE)
            appendLine(provider.knownIds.size.toString(), NamedTextColor.GRAY)
        }
    }
    sender.sendMessage(message)
}

private fun giveExecutor(itemRegistry: BukkitItemsRegistry): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayer(TARGET)
    val amount = IntegerArgumentType.getInteger(context, AMOUNT)
    // We can use only one greedy string at the end, so we read item and its payload from the same argument
    val itemParts = StringArgumentType.getString(context, ITEM).split(" ", limit = 2)
    val item = itemParts.first()
    val payload = itemParts.getOrNull(1)

    val itemStack = itemRegistry.getItem(item, payload, amount)
    if (itemStack != null) {
        target.inventory.addItem(itemStack)
        sender.sendMessage(successText("Gave ${itemStack.amount} [$item] to ${target.name}."))
    } else {
        sender.sendMessage(errorText("Unknown item '$item'"))
    }
}

private const val TARGET = "target"
private const val ITEM = "item"
private const val AMOUNT = "amount"
