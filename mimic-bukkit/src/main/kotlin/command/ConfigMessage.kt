package ru.endlesscode.mimic.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.MimicService
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.config.StringConfigProperty
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.buildTextComponent

@OptIn(ExperimentalMimicApi::class)
internal fun buildConfigMessage(mimic: Mimic): TextComponent = buildTextComponent {
    appendSelectablePropertyConfig(
        property = MimicConfig.LEVEL_SYSTEM,
        selectedId = mimic.getLevelSystemProvider().id,
        services = mimic.getAllLevelSystemProviders(),
    )
    appendSelectablePropertyConfig(
        property = MimicConfig.CLASS_SYSTEM,
        selectedId = mimic.getClassSystemProvider().id,
        services = mimic.getAllClassSystemProviders(),
    )
    appendSelectablePropertyConfig(
        property = MimicConfig.INVENTORY_PROVIDER,
        selectedId = mimic.getPlayerInventoryProvider().id,
        services = mimic.getAllPlayerInventoryProviders(),
    )
}

private fun TextComponent.Builder.appendSelectablePropertyConfig(
    property: StringConfigProperty,
    selectedId: String,
    services: Map<String, MimicService>,
) {
    appendLine()
    appendComment(property.formattedTitle)
    val availableOptions = (services - selectedId).values
    if (availableOptions.isNotEmpty()) {
        appendLine()
        appendComment("Available options: ")
        append(
            availableOptions.toSelectableOptions(
                hint = "Click to select this option",
                command = "/mimic info",
                color = NamedTextColor.GRAY,
            )
        )
    }

    append("\n${property.path}: ", NamedTextColor.GREEN)
    append(selectedId)
    appendLine()
}

private fun Collection<MimicService>.toSelectableOptions(
    hint: String,
    command: String,
    color: TextColor? = null,
) = mapIndexed { index, service ->
    buildTextComponent {
        color(color)
        if (index != 0) append(", ")
        appendClickable(service.id, hint, command)
    }
}

private fun TextComponent.Builder.appendClickable(text: String, hint: String, command: String) {
    append(text, TextDecoration.UNDERLINED)
    hoverEvent(HoverEvent.showText(Component.text(hint)))
    clickEvent(ClickEvent.runCommand(command))
}

private fun TextComponent.Builder.appendComment(comment: String) = append("# $comment", NamedTextColor.GRAY)
