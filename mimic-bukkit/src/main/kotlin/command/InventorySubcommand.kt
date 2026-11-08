package ru.endlesscode.mimic.command

import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.entity.Player
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.internal.text

/**
 * Commands to deal with inventory provider.
 * ```
 * /mimic inventory info (player)
 * ```
 */
internal fun CommandBuilder.inventorySubcommand(mimic: Mimic) {
    for (name in listOf("inventory", "inv")) {
        literal(name) {
            // Show information about player's inventory provider
            literal("info") {
                executesPlayer(infoExecutor(mimic))
                argument(TARGET, ArgumentTypes.player()) {
                    executesPlayer(infoExecutor(mimic))
                }
            }
        }
    }
}

@OptIn(ExperimentalMimicApi::class)
private fun infoExecutor(mimic: Mimic): CommandAction<Player> = { sender, context ->
    val target = context.getPlayerOrDefault(TARGET, sender)
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

private const val TARGET = "target"
