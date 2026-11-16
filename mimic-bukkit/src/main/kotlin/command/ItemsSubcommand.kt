// Copyright (c) 2020 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text
import ru.endlesscode.mimic.items.Items

/**
 * Commands to deal with items.
 * ```
 * /mimic items (info)
 * /mimic items give [target] [item] (amount) (payload)
 * /mimic items compare [item]
 * /mimic items find [item]
 * ```
 */
internal fun CommandBuilder.itemsSubcommand(items: Items) = literal("items") {
    executesAny(infoExecutor(items))

    // Show information about items
    literal("info") {
        executesAny(infoExecutor(items))
    }

    // Give item to player
    literal("give") {
        argument(TARGET, ArgumentTypes.player()) {
            itemArgument(items) {
                executesAny(giveExecutor(items))
                argument(AMOUNT, IntegerArgumentType.integer(1, 64)) {
                    suggestsStrings { listOf("1", "64") }
                    executesAny(giveExecutor(items))
                    argument(PAYLOAD, StringArgumentType.greedyString()) {
                        executesAny(giveExecutor(items))
                    }
                }
            }
        }
    }

    // Compare item in hand with the given item
    literal("compare") {
        itemArgument(items) {
            executesPlayer { sender, context ->
                val itemId = context.getArgument<Key>(ITEM)
                val isSame = items.isItem(sender.requireItemInMainHand(), itemId)
                sender.sendMessage(successText {
                    append("Item in hand ")
                    appendVerdict(isSame, "matches", "doesn't match")
                    append(" ")
                    appendId(itemId.asString())
                    append(".")
                })
            }
        }
    }

    // Check that item with given ID exists
    literal("find") {
        itemArgument(items) {
            executesAny { sender, context ->
                val itemId = context.getArgument<Key>(ITEM)
                val itemExists = items.hasItem(itemId)
                sender.sendMessage(successText {
                    append("Item ")
                    appendId(itemId.asString())
                    append(" ")
                    appendVerdict(itemExists, "exists", "doesn't exist")
                    append(".")
                })
            }
        }
    }
}

private fun CommandBuilder.itemArgument(
    items: Items,
    configure: CommandBuilder.() -> Unit,
) = argument(ITEM, KeyArgumentType("item", "minecraft:diamond")) {
    suggestsIds({ context -> itemHints(items, context.source.sender) }) { items.itemIds() }
    configure()
}

private fun Items.itemIds(): List<String> = getAllItems().map { it.asString() }

/** Suggests ID of the item in hand and namespaces of items. */
private fun itemHints(items: Items, sender: CommandSender): List<SuggestionHint> = listOfNotNull(
    itemInHandId(items, sender)?.let { SuggestionHint(it.asString(), Component.text("Item in hand")) },
) + namespaceHints(items.itemIds()) { "Items from $it" }

/** Returns ID of the item in [sender]'s hand, or `null` if sender isn't a player or the hand is empty. */
private fun itemInHandId(items: Items, sender: CommandSender): Key? =
    (sender as? Player)?.inventory?.itemInMainHand?.let(items::getItemId)

private fun Player.requireItemInMainHand(): ItemStack {
    val item = inventory.itemInMainHand
    if (item.isEmpty) throw commandError("You aren't holding an item.")
    return item
}

private fun infoExecutor(items: Items): CommandAction<CommandSender> = { sender, _ ->
    val allItems = items.getAllItems()

    val message = text {
        appendHeader("Mimic Items")
        appendStats("Items amount" to allItems.size.toString())
        for ((namespace, amount) in allItems.groupingBy { it.namespace() }.eachCount()) {
            appendKey("  $namespace: ")
            appendValue(amount.toString())
            appendLine()
        }
        appendIdStat("Item in hand", itemInHandId(items, sender)?.asString())
        appendCommands(
            suggestedCommand("give", "/mimic items give [target] [item] (amount) (payload)", "Give item to player"),
            suggestedCommand("compare", "/mimic items compare [item]", "Compare item in hand with the given item"),
            suggestedCommand("find", "/mimic items find [item]", "Check that item with the given ID exists"),
        )
    }
    sender.sendMessage(message)
}

private fun giveExecutor(items: Items): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayer(TARGET)
    val itemId = context.getArgument<Key>(ITEM)
    val amount = context.getArgumentOrNull<Int>(AMOUNT) ?: 1
    val payload = context.getArgumentOrNull<String>(PAYLOAD)

    if (!items.hasItem(itemId)) throw commandError("Unknown item '${itemId.asString()}'.")
    val itemStack = items.createItem(itemId, amount, target, payload) ?: run {
        val payloadHint = if (payload != null) " The payload '$payload' may be invalid, see the console." else ""
        throw commandError("Can't create item '${itemId.asString()}'.$payloadHint")
    }
    val givenAmount = itemStack.amount // addItem may change the amount of the passed stack
    target.giveItem(itemStack)
    sender.sendMessage(successText {
        append("Gave $givenAmount ")
        appendId(itemId.asString())
        append(" to ${target.name}.")
    })
    if (givenAmount < amount) {
        sender.sendMessage(warningText("Requested $amount, but only $givenAmount can be given at once."))
    }
}

private const val TARGET = "target"
private const val ITEM = "item"
private const val AMOUNT = "amount"
private const val PAYLOAD = "payload"
