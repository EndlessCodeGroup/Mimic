// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.command

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.command.CommandSender
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text

/**
 * Commands to deal with class systems.
 * ```
 * /mimic class (info) [player]
 * /mimic class check [player] (classes...)
 * ```
 */
internal fun CommandBuilder.classSystemSubcommand(mimic: Mimic) = literal("class") {
    executesPlayer(infoCommandExecutor(mimic))

    // Show information about player's class system
    literal("info") {
        executesPlayer(infoCommandExecutor(mimic))
        argument(TARGET, ArgumentTypes.player()) {
            executesAny(infoCommandExecutor(mimic))
        }
    }

    // Check that player has the given classes
    literal("check") {
        argument(TARGET, ArgumentTypes.player()) {
            argument(CLASSES, StringArgumentType.greedyString()) {
                executesAny(checkCommandExecutor(mimic))
            }
        }
    }
}

private fun infoCommandExecutor(mimic: Mimic): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayerOrSender(TARGET, sender)
    val provider = mimic.getClassSystemProvider()
    val system = provider.getSystem(target)

    val message = text {
        appendHeader("Mimic Classes")
        appendStats(
            "System" to provider.id,
            "Classes" to system.classes.toString(),
            "Primary" to system.primaryClass.toString(),
        )
        appendCommands(
            suggestedCommand("check", "/mimic class check [player] [classes...]", "Check that player has the given classes"),
            suggestedCommand("info", "/mimic class info (player)", "Show information about player's class system"),
        )
    }
    sender.sendMessage(message)
}

private fun checkCommandExecutor(mimic: Mimic): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayer(TARGET)
    val classes = context.getArgument<String>(CLASSES).split(" ")

    val system = mimic.getClassSystem(target)
    val hasAllClasses = system.hasAllClasses(classes)
    val hasAnyOfClasses = system.hasAnyOfClasses(classes)

    val message = successText {
        appendLine("Player '${target.name}':")
        append("- has any of: ")
        appendVerdict(hasAnyOfClasses, "yes", "no")
        appendLine()
        append("- has all: ")
        appendVerdict(hasAllClasses, "yes", "no")
    }
    sender.sendMessage(message)
}

private const val TARGET = "target"
private const val CLASSES = "classes"
