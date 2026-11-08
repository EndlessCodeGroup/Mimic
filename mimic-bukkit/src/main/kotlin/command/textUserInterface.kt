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

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text

internal fun successText(text: String) = Component.text(text, NamedTextColor.GOLD)
internal fun warningText(text: String) = Component.text(text, NamedTextColor.YELLOW)

/** Builds a message with the default success color. */
internal fun successText(builder: TextComponent.Builder.() -> Unit): TextComponent = text {
    color(NamedTextColor.GOLD)
    builder()
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
        Component.text("'$id'")
            .hoverEvent(HoverEvent.showText(Component.text("Click to copy")))
            .clickEvent(ClickEvent.copyToClipboard(id))
    )
}

/** Appends the header separating this message from the previous ones. */
internal fun TextComponent.Builder.appendHeader(title: String) {
    appendLine("----[ $title ]----")
}

internal fun TextComponent.Builder.appendStats(vararg stats: Pair<String, String>) {
    for ((key, value) in stats) {
        append("$key: ", NamedTextColor.DARK_AQUA)
        append(value, NamedTextColor.GRAY)
        appendLine()
    }
}

/** Appends stat with the copyable [id] value. Does nothing if [id] is `null`. */
internal fun TextComponent.Builder.appendIdStat(key: String, id: String?) {
    if (id == null) return
    append("$key: ", NamedTextColor.DARK_AQUA)
    append(text {
        color(NamedTextColor.GRAY)
        appendId(id)
    })
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

private fun descriptionText(description: String) = Component.text(description, NamedTextColor.GRAY)

/** Appends the line with clickable [commands]. */
internal fun TextComponent.Builder.appendCommands(vararg commands: CommandHint) {
    append("Commands:", NamedTextColor.DARK_AQUA)
    for (command in commands) {
        val hover = text {
            appendLine(command.usage, NamedTextColor.YELLOW)
            append(command.description)
        }
        append(" ")
        append(
            Component.text("[${command.label}]", NamedTextColor.YELLOW)
                .hoverEvent(HoverEvent.showText(hover))
                .clickEvent(command.clickEvent)
        )
    }
}
