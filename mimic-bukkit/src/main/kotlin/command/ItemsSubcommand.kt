// Copyright (c) 2020 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.impl.mimic.MimicItemsRegistry
import ru.endlesscode.mimic.impl.mimic.registry
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text
import ru.endlesscode.mimic.items.BukkitItemsRegistry

/**
 * Commands to deal with items registries
 * ```
 * /mimic items (info)
 * /mimic items give [target] [item] (amount) (payload)
 * /mimic items compare [item]
 * /mimic items find [item]
 * ```
 */
internal fun CommandBuilder.itemsSubcommand(itemsRegistry: BukkitItemsRegistry) = literal("items") {
    executesAny(infoExecutor(itemsRegistry))

    // Show information about items service
    literal("info") {
        executesAny(infoExecutor(itemsRegistry))
    }

    // Give item to player
    literal("give") {
        argument(TARGET, ArgumentTypes.player()) {
            itemArgument(itemsRegistry) {
                executesAny(giveExecutor(itemsRegistry))
                argument(AMOUNT, IntegerArgumentType.integer(1, 64)) {
                    suggestsStrings { listOf("1", "64") }
                    executesAny(giveExecutor(itemsRegistry))
                    argument(PAYLOAD, StringArgumentType.greedyString()) {
                        executesAny(giveExecutor(itemsRegistry))
                    }
                }
            }
        }
    }

    // Compare item in hand corresponds and given item
    literal("compare") {
        itemArgument(itemsRegistry) {
            executesPlayer { sender, context ->
                val item = context.getArgument<String>(ITEM)
                val isSame = itemsRegistry.isSameItem(sender.requireItemInMainHand(), item)
                sender.sendMessage(successText {
                    append("Item in hand ")
                    appendVerdict(isSame, "matches", "doesn't match")
                    append(" ")
                    appendId(item)
                    append(".")
                })
            }
        }
    }

    // Check that item with given ID exists
    literal("find") {
        itemArgument(itemsRegistry) {
            executesAny { sender, context ->
                val item = context.getArgument<String>(ITEM)
                val itemExists = itemsRegistry.isItemExists(item)
                sender.sendMessage(successText {
                    append("Item ")
                    appendId(item)
                    append(" ")
                    appendVerdict(itemExists, "exists", "doesn't exist")
                    append(".")
                })
            }
        }
    }
}

private fun CommandBuilder.itemArgument(
    itemRegistry: BukkitItemsRegistry,
    configure: CommandBuilder.() -> Unit,
) = argument(ITEM, ResourceLocationArgumentType) {
    suggestsIds({ sender -> itemHints(itemRegistry, sender) }) { itemRegistry.knownIds }
    configure()
}

/** Suggests ID of the item in hand and namespaces of items registries. */
private fun itemHints(itemRegistry: BukkitItemsRegistry, sender: CommandSender): List<SuggestionHint> {
    val itemInHandId = itemInHandId(itemRegistry, sender)
    val namespaces = (itemRegistry as? MimicItemsRegistry)?.providers.orEmpty().map { it.registry.id }
    val knownIds = itemRegistry.knownIds

    return listOfNotNull(itemInHandId?.let { SuggestionHint(it, Component.text("Item in hand")) }) +
        namespaces.map { SuggestionHint(collapseFolders("$it:", knownIds), Component.text("Items from $it")) }
}

/** Returns ID of the item in [sender]'s hand, or `null` if sender isn't a player or the hand is empty. */
private fun itemInHandId(itemRegistry: BukkitItemsRegistry, sender: CommandSender): String? =
    (sender as? Player)?.inventory?.itemInMainHand
        ?.takeUnless { it.type.isAir }
        ?.let(itemRegistry::getItemId)

private fun Player.requireItemInMainHand(): ItemStack {
    val item = inventory.itemInMainHand
    if (item.type.isAir) throw commandError("You aren't holding an item.")
    return item
}

private fun infoExecutor(itemRegistry: BukkitItemsRegistry): CommandAction<CommandSender> = { sender, _ ->
    val providers = (itemRegistry as? MimicItemsRegistry)?.providers.orEmpty().map { it.registry }

    val message = text {
        appendHeader("Mimic Items")
        appendStats(
            "Item Registry" to itemRegistry.id,
            "Known IDs amount" to itemRegistry.knownIds.size.toString(),
        )

        for (provider in providers) {
            append("  ${provider.id}: ", NamedTextColor.WHITE)
            appendLine(provider.knownIds.size.toString(), NamedTextColor.GRAY)
        }
        appendIdStat("Item in hand", itemInHandId(itemRegistry, sender))
        appendCommands(
            suggestedCommand("give", "/mimic items give [target] [item] (amount) (payload)", "Give item to player"),
            suggestedCommand("compare", "/mimic items compare [item]", "Compare item in hand with the given item"),
            suggestedCommand("find", "/mimic items find [item]", "Check that item with the given ID exists"),
        )
    }
    sender.sendMessage(message)
}

private fun giveExecutor(itemRegistry: BukkitItemsRegistry): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayer(TARGET)
    val item = context.getArgument<String>(ITEM)
    val amount = context.getArgumentOrNull<Int>(AMOUNT) ?: 1
    val payload = context.getArgumentOrNull<String>(PAYLOAD)

    val itemStack = itemRegistry.getItem(item, payload, amount) ?: throw commandError("Unknown item '$item'.")
    val givenAmount = itemStack.amount // addItem may change the amount of the passed stack
    target.giveItem(itemStack)
    sender.sendMessage(successText {
        append("Gave $givenAmount ")
        appendId(item)
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
