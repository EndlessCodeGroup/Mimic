package ru.endlesscode.mimic.command

import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.format.NamedTextColor
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendClickable
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.buildTextComponent

/** Registers command '/mimic' and all subcommands. */
internal fun registerCommand(
    mimic: Mimic,
    config: MimicConfig,
    pluginFullName: String,
    audiences: BukkitAudiences,
) = commandAPICommand("mimic") {
    withPermission("mimic.admin")
    withShortDescription("Show info about Mimic")
    executes(infoExecutor(audiences, pluginFullName))

    configSubcommand(mimic, config, audiences)
    levelSystemSubcommand(mimic)
    classSystemSubcommand(mimic)
}

private fun infoExecutor(audiences: BukkitAudiences, pluginFullName: String) = CommandExecutor { sender, _ ->
    val message = buildTextComponent {
        appendLine(pluginFullName, NamedTextColor.GREEN)
        color(NamedTextColor.GRAY)
        append("Use ")
        append(createClickableCommand())
        append(" to see or change configs")
    }
    audiences.sender(sender).sendMessage(message)
}

private fun createClickableCommand() = buildTextComponent {
    color(NamedTextColor.YELLOW)
    appendClickable(CONFIG_COMMAND, "Click to execute", CONFIG_COMMAND)
}

private const val CONFIG_COMMAND = "/mimic config"
