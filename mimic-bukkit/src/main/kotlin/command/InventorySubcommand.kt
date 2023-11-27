package ru.endlesscode.mimic.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.playerArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.Mimic

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
            sender.send(
                "&3Inventory provider: &7${provider.id}",
                "&3Count of Equipped: &7%d".format(inventory.equippedItems.size),
                "&3Count of Stored: &7%d".format(inventory.storedItems.size),
                "&3Total Count: &7%d".format(inventory.items.size),
            )
        }
    }
}

private const val TARGET = "target"
