// Copyright (c) 2019 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.command

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text
import ru.endlesscode.mimic.level.BukkitLevelSystem

/**
 * Commands to deal with level systems.
 * ```
 * /mimic xp (info) (player)
 * /mimic xp [value] (player)
 * ```
 * Value is `[+|-]<amount>[L|P|T]`: `+` gives, `-` takes, no sign sets.
 * Unit `L` is levels, `P` is points (default), `T` is total exp.
 * For example, `+5L` gives five levels and `100` sets 100 exp points.
 */
internal fun CommandBuilder.levelSystemSubcommand(mimic: Mimic) = literal("xp") {
    executesPlayer(infoCommandExecutor(mimic))

    // Show information about player's level system
    literal("info") {
        executesPlayer(infoCommandExecutor(mimic))
        argument(TARGET, ArgumentTypes.player()) {
            executesAny(infoCommandExecutor(mimic))
        }
    }

    // Give, take or set player's level or exp
    argument(VALUE, ExpValueArgumentType) {
        suggests { _, builder -> suggestValues(builder) }
        executesPlayer(changeCommandExecutor(mimic))
        argument(TARGET, ArgumentTypes.player()) {
            executesAny(changeCommandExecutor(mimic))
        }
    }
}

/** Suggests examples for empty input, and units for an amount without unit. Each suggestion explains the value. */
private fun suggestValues(builder: SuggestionsBuilder) =
    with(builder) {
        val input = remaining
        when {
            input.isEmpty() -> VALUE_EXAMPLES.forEach { suggestValue(it) }
            AMOUNT_PATTERN.matches(input) -> {
                val units = ExpUnit.entries
                    .filterNot { it == ExpUnit.TOTAL && !input.first().isDigit() }
                    .filterNot { it == ExpUnit.LEVELS && '.' in input }
                units.forEach { suggestValue("$input${it.suffix}") }
            }
        }
        buildFuture()
    }

private fun SuggestionsBuilder.suggestValue(text: String) {
    val value = text.toExpValueOrNull()
    if (value == null) suggest(text) else suggest(SuggestionHint(text, value.describe()))
}

private fun ExpValue.describe(): Component {
    val amountText = if (amount % 1 == 0.0) amount.toInt().toString() else amount.toString()
    return text {
        color(NamedTextColor.GRAY)
        if (operation == ExpOperation.SET) {
            append("Set ${unit.title} to ")
            append(amountText, NamedTextColor.YELLOW)
        } else {
            append(if (operation == ExpOperation.GIVE) "Give " else "Take ")
            append(amountText, NamedTextColor.YELLOW)
            append(if (unit == ExpUnit.LEVELS && amount != 1.0) " levels" else " ${unit.title}")
        }
    }
}

private fun infoCommandExecutor(mimic: Mimic): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayerOrSender(TARGET, sender)
    val provider = mimic.getLevelSystemProvider()
    val system = provider.getSystem(target)

    val message = text {
        appendHeader("Mimic XP")
        appendStats(
            "System" to provider.id,
            "Level" to "%.2f".format(system.level + system.fractionalExp),
            "Exp" to "%.1f".format(system.exp),
            "Exp to next level" to "%.1f".format(system.expToNextLevel),
            "Total exp" to "%.1f".format(system.totalExp)
        )
        appendCommands(
            suggestedCommand("change", "/mimic xp [value] (player)", VALUE_DESCRIPTION),
            suggestedCommand("info", "/mimic xp info (player)", "Show information about player's level system"),
        )
    }
    sender.sendMessage(message)
}

private fun changeCommandExecutor(mimic: Mimic): CommandAction<CommandSender> = { sender, context ->
    val value = context.getArgument<ExpValue>(VALUE)
    val target = context.getPlayerOrSender(TARGET, sender)
    catchUnsupported {
        val system = mimic.getLevelSystem(target)
        @Suppress("DEPRECATION") // Allow using exp setter
        when (value.operation) {
            ExpOperation.SET -> when (value.unit) {
                ExpUnit.LEVELS -> system.level = value.levels
                ExpUnit.POINTS -> {
                    // Exp points should be within the current level, the full bar is the next level
                    val maxExp = system.totalExpToNextLevel
                    if (maxExp >= 0 && value.amount >= maxExp) throw pointsAboveLevelError(maxExp)
                    system.exp = value.amount
                }
                ExpUnit.TOTAL -> system.totalExp = value.amount
            }
            ExpOperation.GIVE -> when (value.unit) {
                ExpUnit.LEVELS -> system.giveLevels(value.levels)
                ExpUnit.POINTS -> system.giveExp(value.amount)
                ExpUnit.TOTAL -> throw totalChangeError()
            }
            ExpOperation.TAKE -> when (value.unit) {
                ExpUnit.LEVELS -> system.takeLevels(value.levels)
                ExpUnit.POINTS -> system.takeExp(value.amount)
                ExpUnit.TOTAL -> throw totalChangeError()
            }
        }
        system.printNewStats(sender)
    }
}

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

private enum class ExpOperation { SET, GIVE, TAKE }

private enum class ExpUnit(val suffix: Char, val title: String) {
    LEVELS('L', "level"),
    POINTS('P', "exp points"),
    TOTAL('T', "total exp"),
}

private data class ExpValue(val operation: ExpOperation, val amount: Double, val unit: ExpUnit) {
    val levels: Int get() = amount.toInt()
}

/** Parses value like `+5L`, `-100` or `30T`. Shown to the client as a single word. */
private object ExpValueArgumentType : CustomArgumentType<ExpValue, String> {

    private val invalidValue = DynamicCommandExceptionType { value ->
        LiteralMessage("Invalid value '$value'. Expected amount with optional sign and unit (L, P or T), for example +5L, -100 or 30T.")
    }
    private val fractionalLevels = DynamicCommandExceptionType { value ->
        LiteralMessage("Invalid value '$value'. Levels should be a whole number.")
    }

    override fun parse(reader: StringReader): ExpValue {
        val start = reader.cursor
        val input = reader.readUnquotedString()
        val value = input.toExpValueOrNull()
        if (value == null) {
            reader.cursor = start
            throw invalidValue.createWithContext(reader, input)
        }
        if (value.unit == ExpUnit.LEVELS && value.amount % 1 != 0.0) {
            reader.cursor = start
            throw fractionalLevels.createWithContext(reader, input)
        }
        return value
    }

    override fun getNativeType(): ArgumentType<String> = StringArgumentType.word()
}

/** Parses value like `+5L`, `-100` or `30T`, or returns `null` if the string doesn't match the format. */
private fun String.toExpValueOrNull(): ExpValue? {
    val match = VALUE_PATTERN.matchEntire(this) ?: return null
    val (sign, amountText, suffix) = match.destructured
    val amount = amountText.toDouble().takeIf { it.isFinite() } ?: return null
    val unit =ExpUnit.entries.find { suffix.equals(it.suffix.toString(), ignoreCase = true) } ?: ExpUnit.POINTS
    val operation = when (sign) {
        "+" -> ExpOperation.GIVE
        "-" -> ExpOperation.TAKE
        else -> ExpOperation.SET
    }
    return ExpValue(operation, amount, unit)
}

private val AMOUNT_PATTERN = Regex("[+-]?\\d+(\\.\\d+)?")
private val VALUE_PATTERN = Regex("([+-]?)(\\d+(?:\\.\\d+)?)([lptLPT]?)")
private fun totalChangeError() = commandError("Total exp can only be set or checked, use L or P to give or take exp.")
private fun pointsAboveLevelError(maxExp: Double) =
    commandError("Exp points should be less than %.1f on the current level, use T to set total exp or + to give exp.".format(maxExp))

private val VALUE_DESCRIPTION = text {
    color(NamedTextColor.GRAY)
    append("Sign: ")
    append("+", NamedTextColor.YELLOW)
    append(" gives, ")
    append("-", NamedTextColor.YELLOW)
    appendLine(" takes, no sign sets")
    append("Unit: ")
    append("L", NamedTextColor.YELLOW)
    append(" levels, ")
    append("P", NamedTextColor.YELLOW)
    append(" points (default), ")
    append("T", NamedTextColor.YELLOW)
    appendLine(" total exp")
    append("For example: ")
    append("+5L", NamedTextColor.YELLOW)
    append(", ")
    append("-100", NamedTextColor.YELLOW)
    append(", ")
    append("30T", NamedTextColor.YELLOW)
}

private val VALUE_EXAMPLES =listOf("+1L", "-1L", "10L", "+100", "-100", "100T")

private const val TARGET = "target"
private const val VALUE = "value"
