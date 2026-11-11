@file:OptIn(ExperimentalMimicApi::class)

package ru.endlesscode.mimic.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.blocks.BukkitBlockRegistry
import ru.endlesscode.mimic.blocks.DropsConsumer.Companion.disappear
import ru.endlesscode.mimic.impl.mimic.MimicBlockRegistry
import ru.endlesscode.mimic.impl.mimic.registry
import ru.endlesscode.mimic.impl.mimic.tryGetKnownIds
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text

/**
 * Commands to deal with block registries
 * ```
 * /mimic blocks (info)
 * /mimic blocks give [target] [block] (amount)
 * /mimic blocks set [location] [block] (payload)
 * /mimic blocks break (location)
 * /mimic blocks compare (block)
 * /mimic blocks find [block]
 * ```
 */
@Suppress("UnstableApiUsage")
internal fun CommandBuilder.blocksSubcommand(blockRegistry: BukkitBlockRegistry) = literal("blocks") {
    executesAny(infoExecutor(blockRegistry))

    // Show information about blocks service
    literal("info") {
        executesAny(infoExecutor(blockRegistry))
    }

    // Give item placing the block to player
    literal("give") {
        argument(TARGET, ArgumentTypes.player()) {
            blockArgument(blockRegistry) {
                executesAny(giveExecutor(blockRegistry))
                argument(AMOUNT, IntegerArgumentType.integer(1, 64)) {
                    suggestsStrings { listOf("1", "64") }
                    executesAny(giveExecutor(blockRegistry))
                }
            }
        }
    }

    // Set block at the given location, replacing the existing one
    literal("set") {
        argument(LOCATION, ArgumentTypes.blockPosition()) {
            blockArgument(blockRegistry) {
                executesAny(setExecutor(blockRegistry))
                argument(PAYLOAD, StringArgumentType.greedyString()) {
                    executesAny(setExecutor(blockRegistry))
                }
            }
        }
    }

    // Break block at the given location, or the targeted block, with the item in hand
    literal("break") {
        executesPlayer { sender, _ ->
            val targetBlock = sender.getTargetBlockExact(TARGET_DISTANCE) ?: throw commandError("You aren't looking at a block.")
            breakBlock(blockRegistry, sender, targetBlock.location, sender.inventory.itemInMainHand)
        }
        argument(LOCATION, ArgumentTypes.blockPosition()) {
            executesAny { sender, context ->
                // Non-player senders break the block without a tool
                val tool = (sender as? Player)?.inventory?.itemInMainHand
                breakBlock(blockRegistry, sender, context.getBlockLocation(LOCATION), tool)
            }
        }
    }

    // Compare targeted block with the given block, or with the block placed by item in hand
    literal("compare") {
        executesPlayer { sender, _ ->
            val block = blockRegistry.getBlockIdFromItem(sender.inventory.itemInMainHand)
                ?: throw commandError("Item in hand doesn't place a block.")
            compareTargetedBlock(blockRegistry, sender, block, suffix = " (item in hand)")
        }
        blockArgument(blockRegistry) {
            executesPlayer { sender, context ->
                compareTargetedBlock(blockRegistry, sender, context.getArgument<String>(BLOCK))
            }
        }
    }

    // Check that block with given ID exists
    literal("find") {
        blockArgument(blockRegistry) {
            executesAny { sender, context ->
                val block = context.getArgument<String>(BLOCK)
                val blockExists = blockRegistry.isBlockExists(block)
                sender.sendMessage(successText {
                    append("Block ")
                    appendId(block)
                    append(" ")
                    appendVerdict(blockExists, "exists", "doesn't exist")
                    append(".")
                })
            }
        }
    }
}

private fun CommandBuilder.blockArgument(
    blockRegistry: BukkitBlockRegistry,
    configure: CommandBuilder.() -> Unit,
) = argument(BLOCK, ResourceLocationArgumentType) {
    suggestsIds({ sender -> blockHints(blockRegistry, sender) }) { blockRegistry.knownIds }
    configure()
}

/** Suggests IDs of the block placed by item in hand, of the targeted block, and namespaces of block registries. */
private fun blockHints(blockRegistry: BukkitBlockRegistry, sender: CommandSender): List<SuggestionHint> {
    val namespaces = (blockRegistry as? MimicBlockRegistry)?.providers.orEmpty().map { it.registry.id }
    val knownIds = blockRegistry.knownIds

    return listOfNotNull(
        itemInHandBlockId(blockRegistry, sender)?.let { SuggestionHint(it, Component.text("Block placed by item in hand")) },
        targetedBlockId(blockRegistry, sender)?.let { SuggestionHint(it, Component.text("Targeted block")) },
    ) + namespaces.map { SuggestionHint(collapseFolders("$it:", knownIds), Component.text("Blocks from $it")) }
}

/** Returns ID of the block placed by item in [sender]'s hand, or `null` if there is no such block. */
private fun itemInHandBlockId(blockRegistry: BukkitBlockRegistry, sender: CommandSender): String? =
    (sender as? Player)?.inventory?.itemInMainHand?.let(blockRegistry::getBlockIdFromItem)

/** Returns ID of the block [sender] is looking at, or `null` if there is no such block. */
private fun targetedBlockId(blockRegistry: BukkitBlockRegistry, sender: CommandSender): String? =
    (sender as? Player)?.getTargetBlockExact(TARGET_DISTANCE)?.let(blockRegistry::getBlockId)

private fun infoExecutor(blockRegistry: BukkitBlockRegistry): CommandAction<CommandSender> = { sender, _ ->
    val providers = (blockRegistry as? MimicBlockRegistry)?.providers.orEmpty()

    val message = text {
        appendHeader("Mimic Blocks")
        appendStats(
            "Block Registry" to blockRegistry.id,
            "Known IDs amount" to blockRegistry.knownIds.size.toString(),
        )

        for (provider in providers) {
            append("  ${provider.registry.id}: ", NamedTextColor.WHITE)
            appendLine(provider.tryGetKnownIds().size.toString(), NamedTextColor.GRAY)
        }
        appendIdStat("Targeted block", targetedBlockId(blockRegistry, sender))
        appendIdStat("Item in hand places", itemInHandBlockId(blockRegistry, sender))
        appendCommands(
            suggestedCommand("give", "/mimic blocks give [target] [block] (amount)", "Give item placing the given block to the player"),
            suggestedCommand("set", "/mimic blocks set [location] [block] (payload)", "Set block at the given location, replacing the existing one"),
            suggestedCommand("break", "/mimic blocks break (location)", "Break block at the given location, or the targeted block, with the item in hand"),
            suggestedCommand("compare", "/mimic blocks compare (block)", "Compare targeted block with the given block, or with the block placed by item in hand"),
            suggestedCommand("find", "/mimic blocks find [block]", "Check that block with the given ID exists"),
        )
    }
    sender.sendMessage(message)
}

private fun giveExecutor(blockRegistry: BukkitBlockRegistry): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayer(TARGET)
    val block = context.getArgument<String>(BLOCK)
    val amount = context.getArgumentOrNull<Int>(AMOUNT) ?: 1

    if (!blockRegistry.isBlockExists(block)) throw commandError("Unknown block '$block'.")
    val itemStack = blockRegistry.getBlockItem(block, amount) ?: throw commandError("Block '$block' has no item.")
    val givenAmount = itemStack.amount // addItem may change the amount of the passed stack
    target.giveItem(itemStack)
    sender.sendMessage(successText {
        append("Gave $givenAmount of item placing ")
        appendId(block)
        append(" to ${target.name}.")
    })
    if (givenAmount < amount) {
        sender.sendMessage(warningText("Requested $amount, but only $givenAmount can be given at once."))
    }
}

private fun setExecutor(blockRegistry: BukkitBlockRegistry): CommandAction<CommandSender> = { sender, context ->
    val location = context.getBlockLocation(LOCATION)
    val block = context.getArgument<String>(BLOCK)
    val payload = context.getArgumentOrNull<String>(PAYLOAD)

    if (!blockRegistry.isBlockExists(block)) throw commandError("Unknown block '$block'.")

    val target = location.block
    val position = location.toPosition()
    if (!blockRegistry.setBlock(target, block, payload)) {
        // Replace existing block like vanilla /setblock does: break it without drops and set the new one
        if (target.isEmpty || !blockRegistry.breakBlock(target, dropsConsumer = disappear())) {
            throw commandError("Failed to set '$block' at $position.")
        }
        if (!blockRegistry.setBlock(target, block, payload)) {
            throw commandError("Removed the block at $position, but failed to set '$block'.")
        }
    }

    sender.sendMessage(successText {
        append("Set ")
        appendId(block)
        append(" at $position.")
    })
}

private fun compareTargetedBlock(blockRegistry: BukkitBlockRegistry, sender: Player, block: String, suffix: String = "") {
    val targetBlock = sender.getTargetBlockExact(TARGET_DISTANCE) ?: throw commandError("You aren't looking at a block.")
    val isSame = blockRegistry.isSameBlock(targetBlock, block)
    sender.sendMessage(successText {
        append("Targeted block ")
        appendVerdict(isSame, "matches", "doesn't match")
        append(" ")
        appendId(block)
        append("$suffix.")
    })
}

private fun breakBlock(blockRegistry: BukkitBlockRegistry, sender: CommandSender, location: Location, tool: ItemStack?) {
    val position = location.toPosition()
    // Get ID before breaking as the block is gone after it
    val blockId = blockRegistry.getBlockId(location.block)
    if (!blockRegistry.breakBlock(location.block, tool)) {
        throw commandError("Can't break block at $position.")
    }
    sender.sendMessage(successText {
        append("Broke ")
        if (blockId != null) {
            appendId(blockId)
        } else {
            append("block")
        }
        append(" at $position.")
    })
}

private fun Location.toPosition() = "$blockX $blockY $blockZ"

private const val TARGET = "target"
private const val AMOUNT = "amount"
private const val LOCATION = "location"
private const val BLOCK = "block"
private const val PAYLOAD = "payload"
private const val TARGET_DISTANCE = 8
