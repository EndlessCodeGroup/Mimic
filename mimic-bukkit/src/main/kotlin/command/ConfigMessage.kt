// Copyright (c) 2023 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.command

import net.kyori.adventure.text.TextComponent
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.config.ConfigProperty
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.config.StringConfigProperty
import ru.endlesscode.mimic.config.StringSetConfigProperty
import ru.endlesscode.mimic.impl.vanilla.MinecraftBlockRegistry
import ru.endlesscode.mimic.impl.vanilla.MinecraftItemsRegistry
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text

@OptIn(ExperimentalMimicApi::class)
internal fun buildConfigMessage(mimic: Mimic, config: MimicConfig): TextComponent = text {
    appendHeader("Mimic Config")
    appendSelectablePropertyConfig(
        property = MimicConfig.LEVEL_SYSTEM,
        configuredValue = config.levelSystem,
        selectedOption = mimic.getLevelSystemProvider().id,
        options = mimic.getAllLevelSystemProviders().keys,
    )
    appendSelectablePropertyConfig(
        property = MimicConfig.CLASS_SYSTEM,
        configuredValue = config.classSystem,
        selectedOption = mimic.getClassSystemProvider().id,
        options = mimic.getAllClassSystemProviders().keys,
    )
    appendSelectablePropertyConfig(
        property = MimicConfig.INVENTORY_PROVIDER,
        configuredValue = config.inventoryProvider,
        selectedOption = mimic.getPlayerInventoryProvider().id,
        options = mimic.getAllPlayerInventoryProviders().keys,
    )
    appendSetPropertyConfig(
        property = MimicConfig.DISABLED_ITEMS_REGISTRIES,
        values = config.disabledItemsRegistries,
        options = mimic.getAllItemsRegistries().keys,
        permanentOptions = setOf(MinecraftItemsRegistry.ID),
    )
    appendSetPropertyConfig(
        property = MimicConfig.DISABLED_BLOCK_REGISTRIES,
        values = config.disabledBlockRegistries,
        options = mimic.getAllBlockRegistries().keys,
        permanentOptions = setOf(MinecraftBlockRegistry.ID),
    )
}

private fun TextComponent.Builder.appendSelectablePropertyConfig(
    property: StringConfigProperty,
    configuredValue: String,
    selectedOption: String,
    options: Set<String>,
) = appendProperty(property) {
    val isAutomatic = configuredValue.isEmpty()
    val availableOptions = if (isAutomatic) options else options - selectedOption
    if (availableOptions.isNotEmpty()) {
        appendLine()
        appendComment("Options: ")
        appendSelectableOptions(
            availableOptions,
            hint = "Click to select",
            resolveCommand = { "/$COMMAND_NAME config ${property.path} $it" },
        )
    }

    appendLine()
    appendPropertyPath(property)
    append(selectedOption)
    if (isAutomatic) {
        append(" (auto)", SECONDARY_COLOR)
    } else {
        append(" ")
        appendAction(
            ACTION_RESET,
            hint = "Click to use the implementation with the highest priority",
            command = "/$COMMAND_NAME config $ACTION_RESET ${property.path}",
        )
    }
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
        hint = "Click to disable",
        nonClickableOptions = permanentOptions,
        resolveCommand = { "/$COMMAND_NAME config ${property.path} add $it" },
    )

    appendLine()
    appendPropertyPath(property)
    append("[")
    appendSelectableOptions(values, hint = "Click to enable") {
        "/$COMMAND_NAME config ${property.path} remove $it"
    }
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
    nonClickableOptions: Collection<String> = emptySet(),
    resolveCommand: (String) -> String,
) = options.forEachIndexed { index, option ->
    if (index != 0) append(", ", SECONDARY_COLOR)
    if (option !in nonClickableOptions) {
        appendAction(option, hint, resolveCommand(option))
    } else {
        append(option, SECONDARY_COLOR)
    }
}

private fun TextComponent.Builder.appendPropertyPath(property: ConfigProperty<*>) =
    appendKey("${property.path}: ")

private fun TextComponent.Builder.appendComment(comment: String) = append("# $comment", SECONDARY_COLOR)
