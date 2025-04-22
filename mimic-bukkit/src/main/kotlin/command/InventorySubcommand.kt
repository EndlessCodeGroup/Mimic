package ru.endlesscode.mimic.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.playerArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.internal.text

/**
 * Commands to deal with inventory provider.
 * ```
 * /mimic inventory info (player)
 * ```
 */
@OptIn(ExperimentalMimicApi::class)
internal fun CommandAPICommand.inventorySubcommand(mimic: Mimic) = subcommand("inventory") {
    withAliases("inv")

    subcommand("info") {
        withShortDescription("Show information about player's inventory provider")
        playerArgument(TARGET, optional = true)
        playerExecutor { sender, args ->
            val target = args.getOrDefaultUnchecked(TARGET, sender)
            val provider = mimic.getPlayerInventoryProvider()
            val inventory = provider.getSystem(target)

            val message = text {
                appendStats(
                    "Inventory provider" to provider.id,
                    "Count of Equipped" to inventory.equippedItems.size.toString(),
                    "Count of Stored" to inventory.storedItems.size.toString(),
                    "Total Count" to inventory.items.size.toString(),
                )
            }
            sender.sendMessage(message)
        }
    }
}

private const val TARGET = "target"
