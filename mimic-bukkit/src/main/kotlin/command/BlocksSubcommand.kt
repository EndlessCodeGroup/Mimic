@file:OptIn(ExperimentalMimicApi::class)

package ru.endlesscode.mimic.command

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.command.CommandSender
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
internal fun CommandBuilder.blocksSubcommand(blockRegistry: BukkitBlockRegistry) = literal("blocks") {
    // Show information about blocks service
    literal("info") {
        executesAny(infoExecutor(blockRegistry))
    }

    // Set block at the given location
    literal("set") {
        argument(LOCATION, ArgumentTypes.blockPosition()) {
            blockArgument(blockRegistry) {
                executesAny(setExecutor(blockRegistry))
            }
        }
    }

    // Break block at the given location with the item in hand
    literal("break") {
        argument(LOCATION, ArgumentTypes.blockPosition()) {
            executesPlayer { sender, context ->
                val location = context.getBlockLocation(LOCATION)
                val position = location.toPosition()
                if (blockRegistry.breakBlock(location.block, sender.inventory.itemInMainHand)) {
                    sender.sendMessage(successText("Broke block at $position."))
                } else {
                    sender.sendMessage(errorText("Can't break block at $position"))
                }
            }
        }
    }

    // Compare targeted block and given block
    literal("compare") {
        blockArgument(blockRegistry) {
            executesPlayer { sender, context ->
                val block = StringArgumentType.getString(context, BLOCK)
                val targetBlock = sender.getTargetBlockExact(TARGET_DISTANCE)
                if (targetBlock == null) {
                    sender.sendMessage(errorText("You should look at a block"))
                } else {
                    val isSame = blockRegistry.isSameBlock(targetBlock, block)
                    sender.sendMessage(successText("Targeted block and '$block' %s same.".format(if (isSame) "are" else "aren't")))
                }
            }
        }
    }

    // Prints ID of targeted block and ID of block placed by item in hand
    literal("id") {
        executesPlayer { sender, _ ->
            val blockId = sender.getTargetBlockExact(TARGET_DISTANCE)?.let(blockRegistry::getBlockId)
            val itemBlockId = blockRegistry.getBlockIdFromItem(sender.inventory.itemInMainHand)
            sender.sendMessage(successText("Id of targeted block is '$blockId'"))
            sender.sendMessage(successText("Id of block placed by item in hand is '$itemBlockId'"))
        }
    }

    // Check that block with given ID exists
    literal("find") {
        blockArgument(blockRegistry) {
            executesAny { sender, context ->
                val block = StringArgumentType.getString(context, BLOCK)
                val blockExists = blockRegistry.isBlockExists(block)
                sender.sendMessage(successText("Block with id '$block'%s exists".format(if (blockExists) "" else " isn't")))
            }
        }
    }
}

// See itemArgument in ItemsSubcommand for the reason to use greedy string
private fun CommandBuilder.blockArgument(
    blockRegistry: BukkitBlockRegistry,
    configure: CommandBuilder.() -> Unit,
) = argument(BLOCK, StringArgumentType.greedyString()) {
    suggestsStrings { blockRegistry.knownIds }
    configure()
}

private fun infoExecutor(blockRegistry: BukkitBlockRegistry): CommandAction<CommandSender> = { sender, _ ->
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

private fun setExecutor(blockRegistry: BukkitBlockRegistry): CommandAction<CommandSender> = { sender, context ->
    val location = context.getBlockLocation(LOCATION)
    // We can use only one greedy string at the end, so we read block and its payload from the same argument
    val blockParts = StringArgumentType.getString(context, BLOCK).split(" ", limit = 2)
    val block = blockParts.first()
    val payload = blockParts.getOrNull(1)

    val position = location.toPosition()
    if (blockRegistry.setBlock(location.block, block, payload)) {
        sender.sendMessage(successText("Set [$block] at $position."))
    } else if (!blockRegistry.isBlockExists(block)) {
        sender.sendMessage(errorText("Unknown block '$block'"))
    } else {
        sender.sendMessage(errorText("Failed to set [$block] at $position"))
    }
}

private fun Location.toPosition() = "$blockX $blockY $blockZ"

private const val LOCATION = "location"
private const val BLOCK = "block"
private const val TARGET_DISTANCE = 8
