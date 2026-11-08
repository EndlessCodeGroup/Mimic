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

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text

/**
 * Commands to deal with class systems.
 * ```
 * /mimic class info [player]
 * /mimic class check [player] (classes...)
 * ```
 */
internal fun CommandBuilder.classSystemSubcommand(mimic: Mimic) = literal("class") {
    // Show information about player's class system
    literal("info") {
        executesPlayer(infoCommandExecutor(mimic))
        argument(TARGET, ArgumentTypes.player()) {
            executesPlayer(infoCommandExecutor(mimic))
        }
    }

    // Check that player has the given classes
    literal("check") {
        argument(TARGET, ArgumentTypes.player()) {
            argument(CLASSES, StringArgumentType.greedyString()) {
                executesPlayer(checkCommandExecutor(mimic))
            }
        }
    }
}

private fun infoCommandExecutor(mimic: Mimic): CommandAction<Player> = { player, context ->
    val target = context.getPlayerOrDefault(TARGET, player)
    val provider = mimic.getClassSystemProvider()
    val system = provider.getSystem(target)

    val message = text {
        appendStats(
            "System" to provider.id,
            "Classes" to system.classes.toString(),
            "Primary" to system.primaryClass.toString(),
        )
    }
    player.sendMessage(message)
}

private fun checkCommandExecutor(mimic: Mimic): CommandAction<Player> = { _, context ->
    val target = context.getPlayer(TARGET)
    val classes = StringArgumentType.getString(context, CLASSES).split(" ")

    val system = mimic.getClassSystem(target)
    val hasAllClasses = system.hasAllClasses(classes)
    val hasAnyOfClasses = system.hasAnyOfClasses(classes)

    val message = text {
        color(NamedTextColor.GOLD)
        appendLine("Player '${target.name}':")
        append("- has any of: ")
        appendStatus(hasAnyOfClasses)
        appendLine()
        append("- has all: ")
        appendStatus(hasAllClasses)
    }
    target.sendMessage(message)
}

private fun TextComponent.Builder.appendStatus(status: Boolean) {
    if (status) {
        append("yes", NamedTextColor.GREEN)
    } else {
        append("no", NamedTextColor.RED)
    }
}

private const val TARGET = "target"
private const val CLASSES = "classes"
