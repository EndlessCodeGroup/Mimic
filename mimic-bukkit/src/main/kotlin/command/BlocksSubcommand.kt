// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.blocks.Blocks
import ru.endlesscode.mimic.blocks.DropsConsumer
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text

/**
 * Commands to deal with blocks.
 * ```
 * /mimic blocks (info)
 * /mimic blocks give [target] [block] (amount)
 * /mimic blocks set [location] [block] (state)
 * /mimic blocks break (location)
 * /mimic blocks compare (block)
 * /mimic blocks find [block]
 * ```
 */
@Suppress("UnstableApiUsage")
internal fun CommandBuilder.blocksSubcommand(blocks: Blocks) = literal("blocks") {
    executesAny(infoExecutor(blocks))

    // Show information about blocks
    literal("info") {
        executesAny(infoExecutor(blocks))
    }

    // Give item placing the block to player
    literal("give") {
        argument(TARGET, ArgumentTypes.player()) {
            blockArgument(blocks) {
                executesAny(giveExecutor(blocks))
                argument(AMOUNT, IntegerArgumentType.integer(1, 64)) {
                    suggestsStrings { listOf("1", "64") }
                    executesAny(giveExecutor(blocks))
                }
            }
        }
    }

    // Set block at the given location, replacing the existing one
    literal("set") {
        argument(LOCATION, ArgumentTypes.blockPosition()) {
            blockArgument(blocks) {
                executesAny(setExecutor(blocks))
                argument(STATE, StringArgumentType.greedyString()) {
                    executesAny(setExecutor(blocks))
                }
            }
        }
    }

    // Break block at the given location, or the targeted block, with the item in hand
    literal("break") {
        executesPlayer { sender, _ ->
            val targetBlock = sender.requireTargetBlock()
            breakBlock(blocks, sender, targetBlock.location, sender.inventory.itemInMainHand)
        }
        argument(LOCATION, ArgumentTypes.blockPosition()) {
            executesAny { sender, context ->
                // Non-player senders break the block without a tool
                val tool = (sender as? Player)?.inventory?.itemInMainHand
                breakBlock(blocks, sender, context.getBlockLocation(LOCATION), tool)
            }
        }
    }

    // Compare targeted block with the given block, or with the block placed by item in hand
    literal("compare") {
        executesPlayer { sender, _ ->
            val blockId = blocks.getBlockIdFromItem(sender.inventory.itemInMainHand)
                ?: throw commandError("Item in hand doesn't place a block.")
            compareTargetedBlock(blocks, sender, blockId, suffix = " (item in hand)")
        }
        blockArgument(blocks) {
            executesPlayer { sender, context ->
                compareTargetedBlock(blocks, sender, context.getArgument<Key>(BLOCK))
            }
        }
    }

    // Check that block with given ID exists
    literal("find") {
        blockArgument(blocks) {
            executesAny { sender, context ->
                val blockId = context.getArgument<Key>(BLOCK)
                val blockExists = blocks.hasBlock(blockId)
                sender.sendMessage(successText {
                    append("Block ")
                    appendId(blockId.asString())
                    append(" ")
                    appendVerdict(blockExists, "exists", "doesn't exist")
                    append(".")
                })
            }
        }
    }
}

private fun CommandBuilder.blockArgument(
    blocks: Blocks,
    configure: CommandBuilder.() -> Unit,
) = argument(BLOCK, KeyArgumentType("block", "minecraft:stone")) {
    suggestsIds({ context -> blockHints(blocks, context.source.sender) }) { blocks.blockIds() }
    configure()
}

private fun Blocks.blockIds(): List<String> = getAllBlocks().map { it.asString() }

/** Suggests IDs of the block placed by item in hand, of the targeted block, and namespaces of blocks. */
private fun blockHints(blocks: Blocks, sender: CommandSender): List<SuggestionHint> = listOfNotNull(
    itemInHandBlockId(blocks, sender)?.let { SuggestionHint(it.asString(), Component.text("Block placed by item in hand")) },
    targetedBlockId(blocks, sender)?.let { SuggestionHint(it.asString(), Component.text("Targeted block")) },
) + namespaceHints(blocks.blockIds()) { "Blocks from $it" }

/** Returns ID of the block placed by item in [sender]'s hand, or `null` if there is no such block. */
private fun itemInHandBlockId(blocks: Blocks, sender: CommandSender): Key? =
    (sender as? Player)?.inventory?.itemInMainHand?.let(blocks::getBlockIdFromItem)

/** Returns ID of the block [sender] is looking at, or `null` if there is no such block. */
private fun targetedBlockId(blocks: Blocks, sender: CommandSender): Key? =
    (sender as? Player)?.getTargetBlockExact(TARGET_DISTANCE)?.let(blocks::getBlockId)

private fun infoExecutor(blocks: Blocks): CommandAction<CommandSender> = { sender, _ ->
    val allBlocks = blocks.getAllBlocks()

    val message = text {
        appendHeader("Mimic Blocks")
        appendStats("Blocks amount" to allBlocks.size.toString())
        for ((namespace, amount) in allBlocks.groupingBy { it.namespace() }.eachCount()) {
            appendKey("  $namespace: ")
            appendValue(amount.toString())
            appendLine()
        }
        appendIdStat("Targeted block", targetedBlockId(blocks, sender)?.asString())
        appendIdStat("Item in hand places", itemInHandBlockId(blocks, sender)?.asString())
        appendCommands(
            suggestedCommand("give", "/mimic blocks give [target] [block] (amount)", "Give item placing the given block to the player"),
            suggestedCommand("set", "/mimic blocks set [location] [block] (state)", "Set block at the given location, replacing the existing one"),
            suggestedCommand("break", "/mimic blocks break (location)", "Break block at the given location, or the targeted block, with the item in hand"),
            suggestedCommand("compare", "/mimic blocks compare (block)", "Compare targeted block with the given block, or with the block placed by item in hand"),
            suggestedCommand("find", "/mimic blocks find [block]", "Check that block with the given ID exists"),
        )
    }
    sender.sendMessage(message)
}

private fun giveExecutor(blocks: Blocks): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayer(TARGET)
    val blockId = context.getArgument<Key>(BLOCK)
    val amount = context.getArgumentOrNull<Int>(AMOUNT) ?: 1

    requireKnownBlock(blocks, blockId)
    val itemStack = blocks.createBlockItem(blockId, amount)
        ?: throw commandError("Block '${blockId.asString()}' has no item.")
    val givenAmount = itemStack.amount // addItem may change the amount of the passed stack
    target.giveItem(itemStack)
    sender.sendMessage(successText {
        append("Gave $givenAmount of item placing ")
        appendId(blockId.asString())
        append(" to ${target.name}.")
    })
    if (givenAmount < amount) {
        sender.sendMessage(warningText("Requested $amount, but only $givenAmount can be given at once."))
    }
}

private fun setExecutor(blocks: Blocks): CommandAction<CommandSender> = { sender, context ->
    val location = context.getBlockLocation(LOCATION)
    val blockId = context.getArgument<Key>(BLOCK)
    val state = context.getArgumentOrNull<String>(STATE)

    requireKnownBlock(blocks, blockId)

    val target = location.block
    val position = location.toPosition()
    // Replace existing block like vanilla /setblock does: break it without drops and set the new one
    val isReplaced = !target.isEmpty
    if (isReplaced && !blocks.breakBlock(target, tool = null, DropsConsumer.disappear())) {
        throw commandError("Can't remove the block at $position to set '${blockId.asString()}'.")
    }
    if (!blocks.setBlock(target, blockId, state)) {
        val prefix = if (isReplaced) "Removed the existing block, but failed" else "Failed"
        val stateHint = if (state != null) " The state '$state' may be invalid, see the console." else ""
        throw commandError("$prefix to set '${blockId.asString()}' at $position.$stateHint")
    }

    sender.sendMessage(successText {
        append("Set ")
        appendId(blockId.asString())
        append(" at $position.")
    })
}

private fun compareTargetedBlock(blocks: Blocks, sender: Player, blockId: Key, suffix: String = "") {
    val targetBlock = sender.requireTargetBlock()
    val isSame = blocks.isBlock(targetBlock, blockId)
    sender.sendMessage(successText {
        append("Targeted block ")
        appendVerdict(isSame, "matches", "doesn't match")
        append(" ")
        appendId(blockId.asString())
        append("$suffix.")
    })
}

private fun breakBlock(blocks: Blocks, sender: CommandSender, location: Location, tool: ItemStack?) {
    val position = location.toPosition()
    // Get ID before breaking as the block is gone after it
    val blockId = blocks.getBlockId(location.block)
    if (!blocks.breakBlock(location.block, tool)) {
        throw commandError("Can't break block at $position.")
    }
    sender.sendMessage(successText {
        append("Broke ")
        if (blockId != null) {
            appendId(blockId.asString())
        } else {
            append("block")
        }
        append(" at $position.")
    })
}

private fun requireKnownBlock(blocks: Blocks, blockId: Key) {
    if (!blocks.hasBlock(blockId)) throw commandError("Unknown block '${blockId.asString()}'.")
}

private fun Player.requireTargetBlock(): Block =
    getTargetBlockExact(TARGET_DISTANCE) ?: throw commandError("You aren't looking at a block.")

private fun Location.toPosition() = "$blockX $blockY $blockZ"

private const val TARGET = "target"
private const val AMOUNT = "amount"
private const val LOCATION = "location"
private const val BLOCK = "block"
private const val STATE = "state"
private const val TARGET_DISTANCE = 8
