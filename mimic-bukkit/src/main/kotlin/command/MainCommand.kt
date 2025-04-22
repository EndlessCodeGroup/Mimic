package ru.endlesscode.mimic.command

import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import net.kyori.adventure.text.format.NamedTextColor
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendClickable
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text

internal const val COMMAND_NAME = "mimic"
internal const val COMMAND_INFO_DESCRIPTION = "Show info about Mimic"

/** Registers command '/mimic' and all subcommands. */
internal fun registerCommand(
    mimic: Mimic,
    config: MimicConfig,
    pluginFullName: String,
) = commandAPICommand(COMMAND_NAME) {
    withPermission("mimic.admin")
    withShortDescription(COMMAND_INFO_DESCRIPTION)
    executes(infoExecutor(pluginFullName))

    configSubcommand(mimic, config)
    levelSystemSubcommand(mimic)
    classSystemSubcommand(mimic)
    inventorySubcommand(mimic)
    itemsSubcommand(mimic.getItemsRegistry())
}

private fun infoExecutor(pluginFullName: String) = CommandExecutor { sender, _ ->
    val message = text {
        appendLine(pluginFullName, NamedTextColor.GREEN)
        color(NamedTextColor.GRAY)
        append("Use ")
        append(createClickableCommand())
        append(" to see or change configs")
    }
    sender.sendMessage(message)
}

private fun createClickableCommand() = text {
    color(NamedTextColor.YELLOW)
    appendClickable(CONFIG_COMMAND, "Click to execute", CONFIG_COMMAND)
}

private const val CONFIG_COMMAND = "/$COMMAND_NAME config"
