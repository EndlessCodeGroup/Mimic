// Copyright (c) 2019 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.command

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.internal.text

internal const val COMMAND_NAME = "mimic"
internal const val COMMAND_INFO_DESCRIPTION = "Show info about Mimic"

/** Registers command '/mimic' and all subcommands. */
internal fun JavaPlugin.registerCommand(
    mimic: Mimic,
    config: MimicConfig,
) {
    lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
        val command = buildCommand(mimic, config, pluginFullName = pluginMeta.displayName)
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
        appendHeader(pluginFullName)
        appendCommands(
            executedCommand("config", "/$COMMAND_NAME config", "See or change Mimic config"),
            executedCommand("xp", "/$COMMAND_NAME xp", "Level system"),
            executedCommand("class", "/$COMMAND_NAME class", "Class system"),
            executedCommand("inventory", "/$COMMAND_NAME inventory", "Player inventory provider"),
            executedCommand("items", "/$COMMAND_NAME items", "Items registries"),
            executedCommand("blocks", "/$COMMAND_NAME blocks", "Block registries"),
        )
    }
    sender.sendMessage(message)
}
