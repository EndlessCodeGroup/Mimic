// Copyright (c) 2022 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.command

import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.command.CommandSender
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.internal.text

/**
 * Commands to deal with inventory provider.
 * ```
 * /mimic inventory (info) (player)
 * ```
 */
internal fun CommandBuilder.inventorySubcommand(mimic: Mimic) = literal("inventory") {
    executesPlayer(infoExecutor(mimic))

    // Show information about player's inventory provider
    literal("info") {
        executesPlayer(infoExecutor(mimic))
        argument(TARGET, ArgumentTypes.player()) {
            executesAny(infoExecutor(mimic))
        }
    }
}

@OptIn(ExperimentalMimicApi::class)
private fun infoExecutor(mimic: Mimic): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayerOrSender(TARGET, sender)
    val provider = mimic.getPlayerInventoryProvider()
    val inventory = provider.getSystem(target)

    val message = text {
        appendHeader("Mimic Inventory")
        appendStats(
            "Inventory provider" to provider.id,
            "Count of Equipped" to inventory.equippedItems.size.toString(),
            "Count of Stored" to inventory.storedItems.size.toString(),
            "Total Count" to inventory.items.size.toString(),
        )
        appendCommands(
            suggestedCommand("info", "/mimic inventory info (player)", "Show information about player's inventory provider"),
        )
    }
    sender.sendMessage(message)
}

private const val TARGET = "target"
