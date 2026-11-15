// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.command

import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.command.CommandSender
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.classes.CharacterClass
import ru.endlesscode.mimic.classes.Classes
import ru.endlesscode.mimic.impl.mimic.getPrimaryClassProviderId
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text

/**
 * Commands to deal with classes.
 * ```
 * /mimic classes (info) (player)
 * /mimic classes check [class] (player)
 * /mimic classes set [class] (slot) (player)
 * ```
 */
internal fun CommandBuilder.classesSubcommand(mimic: Mimic) = literal("classes") {
    val classes = mimic.classes
    executesPlayer(infoCommandExecutor(mimic))

    // Show player's classes
    literal("info") {
        executesPlayer(infoCommandExecutor(mimic))
        argument(TARGET, ArgumentTypes.player()) {
            executesAny(infoCommandExecutor(mimic))
        }
    }

    // Check that player has the class or any of its subclasses
    literal("check") {
        classArgument(classes) {
            executesPlayer(checkCommandExecutor(classes))
            argument(TARGET, ArgumentTypes.player()) {
                executesAny(checkCommandExecutor(classes))
            }
        }
    }

    // Set the class in the slot chosen by the plugin or in the given slot.
    // Only the slot argument has server-side suggestions, so it doesn't conflict with the player argument.
    literal("set") {
        classArgument(classes) {
            executesPlayer(setCommandExecutor(classes))
            argument(TARGET, ArgumentTypes.player()) {
                executesAny(setCommandExecutor(classes))
            }
            argument(SLOT, KeyArgumentType("slot", "fabled:race")) {
                suggestsIds({ _ -> namespaceHints(classes.slotIds()) { "Slots from $it" } }) { classes.slotIds() }
                executesPlayer(setCommandExecutor(classes))
                argument(TARGET, ArgumentTypes.player()) {
                    executesAny(setCommandExecutor(classes))
                }
            }
        }
    }
}

private fun CommandBuilder.classArgument(classes: Classes, configure: CommandBuilder.() -> Unit) =
    argument(CLASS, KeyArgumentType("class", "fabled:warrior")) {
        suggestsIds({ _ -> namespaceHints(classes.classIds()) { "Classes from $it" } }) { classes.classIds() }
        configure()
    }

private fun Classes.classIds(): List<String> = getAllClasses().map { it.key().asString() }
private fun Classes.slotIds(): List<String> = getAllSlots().map { it.key().asString() }

private fun infoCommandExecutor(mimic: Mimic): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayerOrSender(TARGET, sender)
    val classes = mimic.classes
    val primaryClass = classes.getPrimaryClass(target)
    val otherClasses = classes.getClasses(target).filterValues { it != primaryClass }
    val slotNames = classes.getAllSlots().associate { it.key() to it.name() }

    val message = text {
        appendHeader("Mimic Classes")
        appendProviderStat("Class provider", mimic.getPrimaryClassProviderId() ?: "none")
        appendKey("${target.name}'s primary class: ")
        if (primaryClass == null) appendValue("none") else appendClass(primaryClass)
        appendLine()
        if (otherClasses.isNotEmpty()) {
            appendKey("Other classes:")
            appendLine()
        }
        for ((namespace, namespaceClasses) in otherClasses.entries.groupBy { it.key.namespace() }) {
            appendValue("  $namespace")
            appendLine()
            for ((slotId, playerClass) in namespaceClasses) {
                append("    ")
                appendNamedId(slotNames[slotId] ?: Component.text(slotId.value()), slotId.asString())
                append(": ")
                appendClass(playerClass)
                appendLine()
            }
        }
        appendCommands(
            suggestedCommand("check", "/mimic classes check [class] (player)", "Check that player has the class or any of its subclasses"),
            suggestedCommand("set", "/mimic classes set [class] (slot) (player)", "Set the class, in the given slot or in the slot chosen by the plugin"),
            suggestedCommand("info", "/mimic classes info (player)", "Show player's classes"),
        )
    }
    sender.sendMessage(message)
}

/** Appends the class name with its ID on hover, and the level track of the class if any. */
private fun TextComponent.Builder.appendClass(playerClass: CharacterClass) {
    appendNamedId(playerClass.name(), playerClass.key().asString())
    val levelTrack = playerClass.levelTrack() ?: return
    append(" · level on ", SECONDARY_COLOR)
    appendId(levelTrack.asString())
}

private fun checkCommandExecutor(classes: Classes): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayerOrSender(TARGET, sender)
    val classId = context.getArgument<Key>(CLASS)
    val hasClass = classes.hasClass(target, classId)

    sender.sendMessage(successText {
        append("${target.name} ")
        appendVerdict(hasClass, "has", "doesn't have")
        append(" class ")
        appendId(classId.asString())
        append(" or its subclass.")
    })
}

private fun setCommandExecutor(classes: Classes): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayerOrSender(TARGET, sender)
    val classId = context.getArgument<Key>(CLASS)
    val slotId = context.getArgumentOrNull<Key>(SLOT)

    val isRequested = if (slotId == null) {
        classes.setClass(target, classId)
    } else {
        classes.setClass(target, classId, slotId)
    }
    if (!isRequested) {
        throw commandError(
            "Can't set class '${classId.asString()}' for ${target.name}: the plugin doesn't support it, " +
                "the class or the slot is unknown, or the player's data isn't loaded."
        )
    }

    sender.sendMessage(successText {
        append("Requested class ")
        appendId(classId.asString())
        append(" for ${target.name}.")
    })
}

private const val TARGET = "target"
private const val CLASS = "class"
private const val SLOT = "slot"
