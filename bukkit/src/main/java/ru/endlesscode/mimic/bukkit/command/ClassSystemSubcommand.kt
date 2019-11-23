/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2018 Osip Fatkullin
 * Copyright (C) 2018 EndlessCode Group and contributors
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
package ru.endlesscode.mimic.bukkit.command

import co.aikar.commands.AbstractCommandManager
import co.aikar.commands.Command
import co.aikar.commands.annotation.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.endlesscode.mimic.api.system.ClassSystem
import ru.endlesscode.mimic.api.system.SystemFactory

@CommandAlias("%command")
@CommandPermission("%perm")
@Subcommand("class|c")
internal class ClassSystemSubcommand(
    private val systemFactory: SystemFactory<ClassSystem>,
    private val util: CommandUtil
) : Command() {

    override fun afterRegister(manager: AbstractCommandManager) {
        manager.getCommandCompletions().registerEnumCompletion<Mode>()
    }

    @Subcommand("info|i")
    @Description("Show information about player's class system")
    @Syntax("[player]")
    @CommandCompletion("@players")
    fun info(sender: CommandSender, @Optional @Flags("other,defaultself") player: Player) {
        val system = systemFactory.get(player)
        util.send(
            sender,
            util.msg("&3System: &7%s", system.name),
            util.msg("&3Classes: &7%s", system.classes),
            util.msg("&3Primary: &7%s", system.primaryClass)
        )
    }

    @Subcommand("has|h")
    @Description("Check that player has given classes")
    @Syntax("<classes> [all|one] [player]")
    @CommandCompletion("@nothing @mode @players")
    fun has(
        sender: CommandSender,
        @Split classes: Array<String>,
        @Default("all") mode: Mode,
        @Optional @Flags("other,defaultself") player: Player
    ) {
        val system = systemFactory.get(player)
        val has = if (mode == Mode.ALL) {
            system.hasAllRequiredClasses(classes.asList())
        } else {
            system.hasOneOfRequiredClasses(classes.asList())
        }
        util.send(sender, util.msg("&6Player '%s' has%s given classes.", player.name, if (has) "" else " not"))
    }

    @Suppress("UNUSED")
    internal enum class Mode { ONE, ALL }
}
