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

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.command.CommandSender
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.internal.Log
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
internal fun CommandAPICommand.levelSystemSubcommand(mimic: Mimic) = subcommand("experience") {
    withAliases("xp")

    subcommand("info") {
        withShortDescription("Show information about player's level system")
        playerArgument(TARGET, optional = true)
        executesPlayer(infoCommandExecutor(mimic))
    }

    subcommand("set") {
        withShortDescription("Set player's level or exp")
        amountArgument()
        valueTypeArgument(allowTotal = true)
        playerArgument(TARGET, optional = true)
        executesPlayer(setCommandExecutor(mimic))
    }

    subcommand("give") {
        withShortDescription("Give level or exp to player")
        amountArgument()
        valueTypeArgument()
        playerArgument(TARGET, optional = true)
        executesPlayer(giveCommandExecutor(mimic))
    }

    subcommand("take") {
        withShortDescription("Take level or exp from player")
        amountArgument()
        valueTypeArgument()
        playerArgument(TARGET, optional = true)
        executesPlayer(takeCommandExecutor(mimic))
    }

    subcommand("has") {
        withShortDescription("Check that player did reach level or has exp")
        amountArgument()
        valueTypeArgument(allowTotal = true)
        playerArgument(TARGET, optional = true)
        executesPlayer(hasCommandExecutor(mimic))
    }
}

private fun CommandAPICommand.amountArgument() = doubleArgument(AMOUNT, min = 0.0) {
    replaceSuggestions(ArgumentSuggestions.strings("1", "10", "100"))
}

private fun CommandAPICommand.valueTypeArgument(
    allowTotal: Boolean = false,
) = stringArgument(TYPE, optional = true) {
    val values = listOfNotNull(TYPE_LVL, TYPE_POINTS, TYPE_TOTAL.takeIf { allowTotal })
    replaceSuggestions(ArgumentSuggestions.stringCollection { values })
}

private fun infoCommandExecutor(mimic: Mimic) = PlayerCommandExecutor { sender, args ->
    val target = args.getOrDefaultUnchecked(TARGET, sender)
    val provider = mimic.getLevelSystemProvider()
    val system = provider.getSystem(target)
    sender.send(
        "&3System: &7${provider.id}",
        "&3Level: &7%.2f".format(system.level + system.fractionalExp),
        "&3Exp: &7%.1f &8| &3To next level: &7%.1f".format(system.exp, system.expToNextLevel),
        "&3Total exp: &7%.1f".format(system.totalExp)
    )
}

private fun setCommandExecutor(mimic: Mimic) = PlayerCommandExecutor { sender, args ->
    val amount: Double by args
    val type = args.getOrDefaultRaw(TYPE, TYPE_LVL)
    val target = args.getOrDefaultUnchecked(TARGET, sender)
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

private fun giveCommandExecutor(mimic: Mimic) = PlayerCommandExecutor { sender, args ->
    val amount: Double by args
    val type = args.getOrDefaultRaw(TYPE, TYPE_LVL)
    val target = args.getOrDefaultUnchecked(TARGET, sender)
    catchUnsupported {
        val system = mimic.getLevelSystem(target)
        when (type) {
            TYPE_LVL -> system.giveLevels(amount.toInt())
            TYPE_POINTS -> system.giveExp(amount)
        }
        system.printNewStats(sender)
    }
}

private fun takeCommandExecutor(mimic: Mimic) = PlayerCommandExecutor { sender, args ->
    val amount: Double by args
    val type = args.getOrDefaultRaw(TYPE, TYPE_LVL)
    val target = args.getOrDefaultUnchecked(TARGET, sender)
    catchUnsupported {
        val system = mimic.getLevelSystem(target)
        when (type) {
            TYPE_LVL -> system.takeLevels(amount.toInt())
            TYPE_POINTS -> system.takeExp(amount)
        }
        system.printNewStats(sender)
    }
}

private inline fun catchUnsupported(block: () -> Unit) {
    try {
        block()
    } catch (e: UnsupportedOperationException) {
        Log.d(e, quiet = true)
        throw CommandAPI.failWithString(e.message.toString())
    }
}

private fun BukkitLevelSystem.printNewStats(sender: CommandSender) {
    sender.send("&6New ${player.name}'s stats: $level LVL, %.1f XP".format(exp))
}

private fun hasCommandExecutor(mimic: Mimic) = PlayerCommandExecutor { sender, args ->
    val amount: Double by args
    val type = args.getOrDefaultRaw(TYPE, TYPE_LVL)
    val target = args.getOrDefaultUnchecked(TARGET, sender)

    fun buildMessage(has: Boolean, valueType: String): String {
        val hasOrNot = if (has) "has" else "has not"
        return "&6${target.name} $hasOrNot $amount $valueType."
    }

    val system = mimic.getLevelSystem(target)
    val message = when (type) {
        TYPE_LVL -> buildMessage(system.didReachLevel(amount.toInt()), "level")
        TYPE_POINTS -> buildMessage(system.hasExp(amount), "points")
        TYPE_TOTAL -> buildMessage(system.hasExpTotal(amount), "total experience")
        else -> error("Unexpected type: $type")
    }
    sender.send(message)
}

private const val TARGET = "target"
private const val AMOUNT = "amount"
private const val TYPE = "type"
private const val TYPE_LVL = "lvl"
private const val TYPE_POINTS = "points"
private const val TYPE_TOTAL = "total"
