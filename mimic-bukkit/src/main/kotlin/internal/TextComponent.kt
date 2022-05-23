package ru.endlesscode.mimic.internal

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

internal fun buildTextComponent(builder: TextComponent.Builder.() -> Unit): TextComponent = Component.text(builder)

internal fun TextComponent.Builder.appendLine(
    text: String,
    color: TextColor? = null,
    vararg decorations: TextDecoration,
): TextComponent.Builder = append(text, color, *decorations).appendLine()

internal fun TextComponent.Builder.appendLine(): TextComponent.Builder = append("\n")

internal fun TextComponent.Builder.append(
    text: String,
    color: TextColor? = null,
    vararg decorations: TextDecoration,
): TextComponent.Builder {
    return append(Component.text(text, color, *decorations))
}

internal fun TextComponent.Builder.append(text: String, vararg decorations: TextDecoration): TextComponent.Builder {
    return append(Component.text(text, null, *decorations))
}

internal fun TextComponent.Builder.appendClickable(text: String, hint: String, command: String) {
    append(text, TextDecoration.UNDERLINED)
    hoverEvent(HoverEvent.showText(Component.text(hint)))
    clickEvent(ClickEvent.runCommand(command))
}
