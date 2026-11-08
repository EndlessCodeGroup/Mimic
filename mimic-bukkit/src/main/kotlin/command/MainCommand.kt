package ru.endlesscode.mimic.command

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.mimic.ExperimentalMimicApi
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
    plugin: JavaPlugin,
    mimic: Mimic,
    config: MimicConfig,
) {
    plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
        val command = buildCommand(mimic, config, pluginFullName = plugin.pluginMeta.displayName)
        event.registrar().register(command, COMMAND_INFO_DESCRIPTION)
    }
}

private fun buildCommand(
    mimic: Mimic,
    config: MimicConfig,
    pluginFullName: String,
) = Commands.literal(COMMAND_NAME).apply {
    requires { it.sender.hasPermission("mimic.admin") }
    executesAny(infoExecutor(pluginFullName))

    configSubcommand(mimic, config)
    levelSystemSubcommand(mimic)
    classSystemSubcommand(mimic)
    inventorySubcommand(mimic)
    itemsSubcommand(mimic.getItemsRegistry())
    @OptIn(ExperimentalMimicApi::class)
    blocksSubcommand(mimic.getBlockRegistry())
}.build()

private fun infoExecutor(pluginFullName: String): CommandAction<CommandSender> = { sender, _ ->
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
