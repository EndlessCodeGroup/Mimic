// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendClickable
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text

// Colors used in command messages. Each color has a single meaning.
private val HEADER_COLOR = NamedTextColor.GOLD
private val KEY_COLOR = NamedTextColor.AQUA
private val VALUE_COLOR = NamedTextColor.WHITE
private val ID_COLOR = NamedTextColor.GREEN
private val ACTION_COLOR = NamedTextColor.YELLOW
internal val SECONDARY_COLOR = NamedTextColor.GRAY
private val WARNING_COLOR = NamedTextColor.GOLD

internal fun successText(text: String) = Component.text(text, VALUE_COLOR)
internal fun warningText(text: String) = Component.text(text, WARNING_COLOR)

/** Builds a message with the default success color. */
internal fun successText(builder: TextComponent.Builder.() -> Unit): TextComponent = text {
    color(VALUE_COLOR)
    builder()
}

/** Appends a key of key-value pair. */
internal fun TextComponent.Builder.appendKey(key: String) = append(key, KEY_COLOR)

/** Appends a value highlighted inside of secondary text. */
internal fun TextComponent.Builder.appendValue(value: String) = append(value, VALUE_COLOR)

/** Appends underlined [label] running the [command] on click. */
internal fun TextComponent.Builder.appendAction(label: String, hint: String, command: String) {
    append(text {
        color(ACTION_COLOR)
        appendClickable(label, hint, command)
    })
}

/** Appends [positive] text in green if [isPositive], otherwise appends [negative] text in red. */
internal fun TextComponent.Builder.appendVerdict(isPositive: Boolean, positive: String, negative: String) {
    if (isPositive) {
        append(positive, NamedTextColor.GREEN)
    } else {
        append(negative, NamedTextColor.RED)
    }
}

/** Appends quoted [id] which is copied to clipboard on click. */
internal fun TextComponent.Builder.appendId(id: String) {
    append(
        Component.text("'$id'", ID_COLOR)
            .hoverEvent(HoverEvent.showText(Component.text("Click to copy")))
            .clickEvent(ClickEvent.copyToClipboard(id))
    )
}

/** Appends the header separating this message from the previous ones. */
internal fun TextComponent.Builder.appendHeader(title: String) {
    appendLine("----[ $title ]----", HEADER_COLOR)
}

internal fun TextComponent.Builder.appendStats(vararg stats: Pair<String, String>) {
    for ((key, value) in stats) {
        appendKey("$key: ")
        appendValue(value)
        appendLine()
    }
}

/** Appends stat with the copyable [id] value. Does nothing if [id] is `null`. */
internal fun TextComponent.Builder.appendIdStat(key: String, id: String?) {
    if (id == null) return
    appendKey("$key: ")
    appendId(id)
    appendLine()
}

/** Command shown in info output with its [usage] and [description] on hover. */
internal class CommandHint(val label: String, val usage: String, val description: Component, val clickEvent: ClickEvent)

/**
 * Hint for a command expecting arguments. On click, puts the command into the chat input.
 * [usage] uses `[required]` and `(optional)` notation for arguments.
 */
internal fun suggestedCommand(label: String, usage: String, description: String): CommandHint =
    suggestedCommand(label, usage, descriptionText(description))

internal fun suggestedCommand(label: String, usage: String, description: Component): CommandHint {
    val command = usage.substringBefore(" [").substringBefore(" (")
    return CommandHint(label, usage, description, ClickEvent.suggestCommand("$command "))
}

/** Hint for a command without arguments. Runs the [command] on click. */
internal fun executedCommand(label: String, command: String, description: String): CommandHint =
    CommandHint(label, command, descriptionText(description), ClickEvent.runCommand(command))

private fun descriptionText(description: String) = Component.text(description, SECONDARY_COLOR)

/** Appends the line with clickable [commands]. */
internal fun TextComponent.Builder.appendCommands(vararg commands: CommandHint) {
    appendKey("Commands:")
    for (command in commands) {
        val hover = text {
            appendLine(command.usage, ACTION_COLOR)
            append(command.description)
        }
        append(" ")
        append(
            Component.text("[${command.label}]", ACTION_COLOR)
                .hoverEvent(HoverEvent.showText(hover))
                .clickEvent(command.clickEvent)
        )
    }
}
