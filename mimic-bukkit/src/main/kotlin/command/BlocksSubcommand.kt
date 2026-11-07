@file:OptIn(ExperimentalMimicApi::class)

package ru.endlesscode.mimic.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.LocationType
import dev.jorel.commandapi.executors.CommandArguments
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.kotlindsl.*
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.blocks.BukkitBlockRegistry
import ru.endlesscode.mimic.blocks.unwrap
import ru.endlesscode.mimic.impl.mimic.MimicBlockRegistry
import ru.endlesscode.mimic.impl.mimic.registry
import ru.endlesscode.mimic.impl.mimic.tryGetKnownIds
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text

/**
 * Commands to deal with block registries
 * ```
 * /mimic blocks info
 * /mimic blocks set [location] [block] (payload)
 * /mimic blocks break [location]
 * /mimic blocks compare [block]
 * /mimic blocks id
 * /mimic blocks find [block]
 * ```
 */
internal fun CommandAPICommand.blocksSubcommand(blockRegistry: BukkitBlockRegistry) = subcommand("blocks") {
    subcommand("info") {
        withShortDescription("Show information about blocks service")
        executes(infoExecutor(blockRegistry))
    }

    subcommand("set") {
        withShortDescription("Set block at the given location")
        locationArgument(LOCATION, LocationType.BLOCK_POSITION)
        blockArgument(blockRegistry)
        executes(setExecutor(blockRegistry))
    }

    subcommand("break") {
        withShortDescription("Break block at the given location with the item in hand")
        locationArgument(LOCATION, LocationType.BLOCK_POSITION)
        playerExecutor { sender, args ->
            val location: Location by args
            val position = location.toPosition()
            if (blockRegistry.breakBlock(location.block, sender.inventory.itemInMainHand)) {
                sender.sendMessage(successText("Broke block at $position."))
            } else {
                sender.sendMessage(errorText("Can't break block at $position"))
            }
        }
    }

    subcommand("compare") {
        withShortDescription("Compare targeted block and given block")
        blockArgument(blockRegistry)
        playerExecutor { sender, args ->
            val block: String by args
            val targetBlock = sender.getTargetBlockExact(TARGET_DISTANCE)
            if (targetBlock == null) {
                sender.sendMessage(errorText("You should look at a block"))
            } else {
                val isSame = blockRegistry.isSameBlock(targetBlock, block)
                sender.sendMessage(successText("Targeted block and '$block' %s same.".format(if (isSame) "are" else "aren't")))
            }
        }
    }

    subcommand("id") {
        withShortDescription("Prints ID of targeted block and ID of block placed by item in hand")
        playerExecutor { sender, _ ->
            val blockId = sender.getTargetBlockExact(TARGET_DISTANCE)?.let(blockRegistry::getBlockId)
            val itemBlockId = blockRegistry.getBlockIdFromItem(sender.inventory.itemInMainHand)
            sender.sendMessage(successText("Id of targeted block is '$blockId'"))
            sender.sendMessage(successText("Id of block placed by item in hand is '$itemBlockId'"))
        }
    }

    subcommand("find") {
        withShortDescription("Check that block with given ID exists")
        blockArgument(blockRegistry)
        anyExecutor { sender, args ->
            val block: String by args
            val blockExists = blockRegistry.isBlockExists(block)
            sender.sendMessage(successText("Block with id '$block'%s exists".format(if (blockExists) "" else " isn't")))
        }
    }
}

// See itemArgument in ItemsSubcommand for the reason to use greedy string
private fun CommandAPICommand.blockArgument(blockRegistry: BukkitBlockRegistry) = greedyStringArgument(BLOCK) {
    replaceSuggestions(ArgumentSuggestions.stringCollection { blockRegistry.knownIds })
}

private fun infoExecutor(blockRegistry: BukkitBlockRegistry) = CommandExecutor { sender, _ ->
    val providers = (blockRegistry.unwrap() as? MimicBlockRegistry)?.providers.orEmpty()

    val message = text {
        appendStats(
            "Block Registry" to blockRegistry.id,
            "Known IDs amount" to blockRegistry.knownIds.size.toString(),
        )

        for (provider in providers) {
            append("  ${provider.registry.id}: ", NamedTextColor.WHITE)
            appendLine(provider.tryGetKnownIds().size.toString(), NamedTextColor.GRAY)
        }
    }
    sender.sendMessage(message)
}

private fun setExecutor(blockRegistry: BukkitBlockRegistry) = CommandExecutor { sender, args ->
    val location: Location by args
    val (block, payload) = args.readBlockAndPayload()

    val position = location.toPosition()
    if (blockRegistry.setBlock(location.block, block, payload)) {
        sender.sendMessage(successText("Set [$block] at $position."))
    } else if (!blockRegistry.isBlockExists(block)) {
        sender.sendMessage(errorText("Unknown block '$block'"))
    } else {
        sender.sendMessage(errorText("Failed to set [$block] at $position"))
    }
}

// We can use only one greedy string at the end, so we read block and its payload from the same argument
private fun CommandArguments.readBlockAndPayload(): Pair<String, String?> {
    val blockParts = getRaw(BLOCK).orEmpty().split(" ", limit = 2)
    return blockParts.first() to blockParts.getOrNull(1)
}

private fun Location.toPosition() = "$blockX $blockY $blockZ"

private const val LOCATION = "location"
private const val BLOCK = "block"
private const val TARGET_DISTANCE = 8
