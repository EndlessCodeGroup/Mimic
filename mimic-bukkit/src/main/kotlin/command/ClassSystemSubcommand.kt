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

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import dev.jorel.commandapi.kotlindsl.playerArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import ru.endlesscode.mimic.Mimic

/**
 * Commands to deal with class systems.
 * ```
 * /mimic class info [player]
 * /mimic class check [player] (classes...)
 * ```
 */
internal fun CommandAPICommand.classSystemSubcommand(mimic: Mimic) = subcommand("class") {
    subcommand("info") {
        withShortDescription("Show information about player's class system")
        playerArgument(TARGET, optional = true)
        executesPlayer(infoCommandExecutor(mimic))
    }

    subcommand("check") {
        withShortDescription("Check that player has the given classes")
        playerArgument(TARGET)
        greedyStringArgument(CLASSES)
        executesPlayer(checkCommandExecutor(mimic))
    }
}

private fun infoCommandExecutor(mimic: Mimic) = PlayerCommandExecutor { player, args ->
    val target = args.getOrDefaultUnchecked(TARGET, player)
    val provider = mimic.getClassSystemProvider()
    val system = provider.getSystem(target)
    player.send(
        "&3System: &7${provider.id}",
        "&3Classes: &7${system.classes}",
        "&3Primary: &7${system.primaryClass}",
    )
}

private fun checkCommandExecutor(mimic: Mimic) = PlayerCommandExecutor { player, args ->
    val target = args.getOrDefaultUnchecked(TARGET, player)
    val classes = args.getOrDefaultRaw(CLASSES, "").split(" ")

    val system = mimic.getClassSystem(target)
    val hasAllClasses = system.hasAllClasses(classes)
    val hasAnyOfClasses = system.hasAnyOfClasses(classes)
    target.send(
        "&6Player '${target.name}':",
        "&6- has any of: ${hasAnyOfClasses.toChatMessage()}",
        "&6- has all: ${hasAllClasses.toChatMessage()}",
    )
}

private fun Boolean.toChatMessage(): String = if (this) "&ayes" else "&cno"

private const val TARGET = "target"
private const val CLASSES = "classes"
