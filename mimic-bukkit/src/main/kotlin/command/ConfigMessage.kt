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
import ru.endlesscode.mimic.config.ConfigProperty
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.config.StringConfigProperty
import ru.endlesscode.mimic.config.StringSetConfigProperty
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.buildTextComponent

@OptIn(ExperimentalMimicApi::class)
internal fun buildConfigMessage(mimic: Mimic, config: MimicConfig): TextComponent = buildTextComponent {
    appendSelectablePropertyConfig(
        property = MimicConfig.LEVEL_SYSTEM,
        selectedOption = mimic.getLevelSystemProvider().id,
        options = mimic.getAllLevelSystemProviders().keys,
    )
    appendSelectablePropertyConfig(
        property = MimicConfig.CLASS_SYSTEM,
        selectedOption = mimic.getClassSystemProvider().id,
        options = mimic.getAllClassSystemProviders().keys,
    )
    appendSelectablePropertyConfig(
        property = MimicConfig.INVENTORY_PROVIDER,
        selectedOption = mimic.getPlayerInventoryProvider().id,
        options = mimic.getAllPlayerInventoryProviders().keys,
    )
    appendSetPropertyConfig(
        property = MimicConfig.DISABLED_ITEMS_REGISTRIES,
        values = config.disabledItemsRegistries,
        options = mimic.getAllItemsRegistries().keys,
        permanentOptions = MimicConfig.DEFAULT_ITEMS_REGISTRIES,
    )
}

private fun TextComponent.Builder.appendSelectablePropertyConfig(
    property: StringConfigProperty,
    selectedOption: String,
    options: Set<String>,
) = appendProperty(property) {
    val availableOptions = options - selectedOption
    if (availableOptions.isNotEmpty()) {
        appendLine()
        appendComment("Options: ")
        appendSelectableOptions(
            availableOptions,
            hint = "Click to select this option",
            command = "/mimic info",
            color = NamedTextColor.GRAY,
        )
    }

    appendLine()
    appendPropertyPath(property)
    append(selectedOption)
}

private fun TextComponent.Builder.appendSetPropertyConfig(
    property: StringSetConfigProperty,
    values: Set<String>,
    options: Set<String>,
    permanentOptions: Set<String>,
) = appendProperty(property) {
    val selectableIds = options - values
    appendLine()
    appendComment("Options: ")
    appendSelectableOptions(
        selectableIds,
        hint = "Click to add",
        command = "/mimic info",
        color = NamedTextColor.GRAY,
        nonClickableOptions = permanentOptions,
    )

    appendLine()
    appendPropertyPath(property)
    append("[")
    appendSelectableOptions(
        values,
        hint = "Click to remove",
        command = "/mimic info",
    )
    append("]")
}

private fun TextComponent.Builder.appendProperty(property: ConfigProperty<*>, body: TextComponent.Builder.() -> Unit) {
    appendLine()
    appendComment(property.formattedTitle)
    body()
    appendLine()
}

private fun TextComponent.Builder.appendSelectableOptions(
    options: Collection<String>,
    hint: String,
    command: String,
    color: TextColor? = null,
    nonClickableOptions: Collection<String> = emptySet(),
) = append(
    options.mapIndexed { index, option ->
        buildTextComponent {
            color(color)
            if (index != 0) append(", ")
            if (option !in nonClickableOptions) {
                appendClickable(option, hint, command)
            } else {
                append(option)
            }
        }
    }
)

private fun TextComponent.Builder.appendClickable(text: String, hint: String, command: String) {
    append(text, TextDecoration.UNDERLINED)
    hoverEvent(HoverEvent.showText(Component.text(hint)))
    clickEvent(ClickEvent.runCommand(command))
}

private fun TextComponent.Builder.appendPropertyPath(property: ConfigProperty<*>) =
    append("${property.path}: ", NamedTextColor.GREEN)

private fun TextComponent.Builder.appendComment(comment: String) = append("# $comment", NamedTextColor.GRAY)
