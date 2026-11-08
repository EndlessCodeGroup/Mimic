/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
 *
 * BukkitMimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BukkitMimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BukkitMimic.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.command

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.text.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.internal.text
import ru.endlesscode.mimic.level.BukkitLevelSystem
import kotlin.math.roundToInt

/**
 * Commands to deal with level systems.
 * ```
 * /mimic xp info (player)
 * /mimic xp set [amount] (lvl|points|total) (player)
 * /mimic xp give [amount] (lvl|points) (player)
 * /mimic xp take [amount] (lvl|points) (player)
 * /mimic xp has [amount] (lvl|points|total) (player)
 * ```
 */
internal fun CommandBuilder.levelSystemSubcommand(mimic: Mimic) {
    for (name in listOf("experience", "xp")) {
        literal(name) {
            // Show information about player's level system
            literal("info") {
                executesPlayer(infoCommandExecutor(mimic))
                argument(TARGET, ArgumentTypes.player()) {
                    executesPlayer(infoCommandExecutor(mimic))
                }
            }

            // Set player's level or exp
            literal("set") {
                amountTypeTargetArguments(allowTotal = true, setCommandExecutor(mimic))
            }

            // Give level or exp to player
            literal("give") {
                amountTypeTargetArguments(allowTotal = false, giveCommandExecutor(mimic))
            }

            // Take level or exp from player
            literal("take") {
                amountTypeTargetArguments(allowTotal = false, takeCommandExecutor(mimic))
            }

            // Check that player did reach level or has exp
            literal("has") {
                amountTypeTargetArguments(allowTotal = true, hasCommandExecutor(mimic))
            }
        }
    }
}

/** Adds arguments `[amount] (type) (target)`, where type and target are optional. */
private fun CommandBuilder.amountTypeTargetArguments(allowTotal: Boolean, action: CommandAction<Player>) {
    argument(AMOUNT, DoubleArgumentType.doubleArg(0.0)) {
        suggestsStrings { listOf("1", "10", "100") }
        executesPlayer(action)

        argument(TYPE, StringArgumentType.word()) {
            val values = listOfNotNull(TYPE_LVL, TYPE_POINTS, TYPE_TOTAL.takeIf { allowTotal })
            suggestsStrings { values }
            executesPlayer(action)

            argument(TARGET, ArgumentTypes.player()) {
                executesPlayer(action)
            }
        }
    }
}

private fun infoCommandExecutor(mimic: Mimic): CommandAction<Player> = { sender, context ->
    val target = context.getPlayerOrDefault(TARGET, sender)
    val provider = mimic.getLevelSystemProvider()
    val system = provider.getSystem(target)

    val message = text {
        appendStats(
            "System" to provider.id,
            "Level" to "%.2f".format(system.level + system.fractionalExp),
            "Exp" to "%.1f".format(system.exp),
            "Exp to next level" to "%.1f".format(system.expToNextLevel),
            "Total exp" to "%.1f".format(system.totalExp)
        )
    }
    target.sendMessage(message)
}

private fun setCommandExecutor(mimic: Mimic): CommandAction<Player> = { sender, context ->
    val amount = DoubleArgumentType.getDouble(context, AMOUNT)
    val type = context.getTypeOrDefault()
    val target = context.getPlayerOrDefault(TARGET, sender)
    catchUnsupported {
        val system = mimic.getLevelSystem(target)
        @Suppress("DEPRECATION") // Allow using exp setter
        when (type) {
            TYPE_LVL -> system.level = amount.roundToInt()
            TYPE_POINTS -> system.exp = amount
            TYPE_TOTAL -> system.totalExp = amount
        }
        system.printNewStats(sender)
    }
}

private fun giveCommandExecutor(mimic: Mimic): CommandAction<Player> = { sender, context ->
    val amount = DoubleArgumentType.getDouble(context, AMOUNT)
    val type = context.getTypeOrDefault()
    val target = context.getPlayerOrDefault(TARGET, sender)
    catchUnsupported {
        val system = mimic.getLevelSystem(target)
        when (type) {
            TYPE_LVL -> system.giveLevels(amount.toInt())
            TYPE_POINTS -> system.giveExp(amount)
        }
        system.printNewStats(sender)
    }
}

private fun takeCommandExecutor(mimic: Mimic): CommandAction<Player> = { sender, context ->
    val amount = DoubleArgumentType.getDouble(context, AMOUNT)
    val type = context.getTypeOrDefault()
    val target = context.getPlayerOrDefault(TARGET, sender)
    catchUnsupported {
        val system = mimic.getLevelSystem(target)
        when (type) {
            TYPE_LVL -> system.takeLevels(amount.toInt())
            TYPE_POINTS -> system.takeExp(amount)
        }
        system.printNewStats(sender)
    }
}

private fun CommandContext<*>.getTypeOrDefault(): String =
    if (hasArgument(TYPE)) StringArgumentType.getString(this, TYPE) else TYPE_LVL

private inline fun catchUnsupported(block: () -> Unit) {
    try {
        block()
    } catch (e: UnsupportedOperationException) {
        Log.d(e, quiet = true)
        throw commandError(e.message.toString())
    }
}

private fun BukkitLevelSystem.printNewStats(sender: CommandSender) {
    sender.sendMessage(successText("New ${player.name}'s stats: $level LVL, %.1f XP".format(exp)))
}

private fun hasCommandExecutor(mimic: Mimic): CommandAction<Player> = { sender, context ->
    val amount = DoubleArgumentType.getDouble(context, AMOUNT)
    val type = context.getTypeOrDefault()
    val target = context.getPlayerOrDefault(TARGET, sender)

    fun buildMessage(has: Boolean, valueType: String): TextComponent {
        val hasOrNot = if (has) "has" else "has not"
        return successText("${target.name} $hasOrNot $amount $valueType.")
    }

    val system = mimic.getLevelSystem(target)
    val message = when (type) {
        TYPE_LVL -> buildMessage(system.didReachLevel(amount.toInt()), "level")
        TYPE_POINTS -> buildMessage(system.hasExp(amount), "points")
        TYPE_TOTAL -> buildMessage(system.hasExpTotal(amount), "total experience")
        else -> error("Unexpected type: $type")
    }
    sender.sendMessage(message)
}

private const val TARGET = "target"
private const val AMOUNT = "amount"
private const val TYPE = "type"
private const val TYPE_LVL = "lvl"
private const val TYPE_POINTS = "points"
private const val TYPE_TOTAL = "total"
