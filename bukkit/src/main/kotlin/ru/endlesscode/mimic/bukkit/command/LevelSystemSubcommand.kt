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
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.MessageKeys
import co.aikar.commands.MimicCommand
import co.aikar.commands.annotation.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.endlesscode.mimic.api.system.LevelSystem
import ru.endlesscode.mimic.api.system.SystemFactory
import ru.endlesscode.mimic.bukkit.util.Log

@CommandAlias("%command")
@CommandPermission("%perm")
@Subcommand("level|lvl|l|experience|exp|xp|e")
internal class LevelSystemSubcommand(
    private val systemFactory: SystemFactory<LevelSystem>
) : MimicCommand() {

    companion object {
        private val NUMBER get() = Regex("\\d+")
        private val SIGN_NUMBER get() = Regex("[-+]?\\d+")
        private val SIGN_NUMBER_PERCENT get() = Regex("[-+]?\\d+%?")
    }

    override fun afterRegister(manager: AbstractCommandManager) {
        manager.getCommandCompletions().registerEnumCompletion<ValueType>("type")
    }

    @Subcommand("info|i")
    @Description("Show information about player's level system")
    @CommandCompletion("@players")
    fun info(sender: CommandSender, @Optional @Flags("other,defaultself") player: Player) {
        val system = systemFactory.get(player)
        sender.send(
            "&3System: &7${system.name}",
            "&3Level: &7%.2f".format(system.level + system.fractionalExp),
            "&3Exp: &7%.1f &8| &3To next level: &7%.1f".format(system.exp, system.expToNextLevel),
            "&3Total exp: &7%.1f".format(system.totalExp)
        )
    }

    @Subcommand("set|s")
    @Description("Change player's level, exp or total exp")
    @CommandCompletion("@nothing @type @players")
    fun set(
        sender: CommandSender,
        value: String,
        @Default("exp") type: ValueType,
        @Optional @Flags("other,defaultself") player: Player
    ) {
        try {
            val ls = systemFactory.get(player)
            when (type) {
                ValueType.LVL -> {
                    setLevel(ls, value)
                    sender.send("&6New ${player.name}'s level is ${ls.level}")
                }
                ValueType.TOTAL -> {
                    setTotalExp(ls, value)
                    sender.send("&6New ${player.name}'s total exp is %.1f (${ls.level} lvl)".format(ls.totalExp))
                }
                ValueType.EXP -> {
                    setExp(ls, value)
                    val level = ls.level + ls.fractionalExp
                    sender.send("&6New ${player.name}'s exp is %.1f (%.2f lvl)".format(ls.exp, level))
                }
            }
        } catch (e: UnsupportedOperationException) {
            Log.d(e, quiet = true)
            wrongArgument(e.message.toString(), showSyntax = false)
        }
    }

    @Subcommand("has|h|reach|r")
    @Description("Check that player did reach level or has exp")
    @CommandCompletion("@nothing @type @players")
    fun has(
        sender: CommandSender,
        value: Int,
        @Default("lvl") type: ValueType,
        @Optional @Flags("other,defaultself") player: Player
    ) {
        val system = systemFactory.get(player)
        val has: Boolean
        val message: String
        when (type) {
            ValueType.LVL -> {
                has = system.didReachLevel(value)
                message = "&6%s did%s reach %d lvl."
            }
            ValueType.EXP -> {
                has = system.hasExp(value.toDouble())
                message = "&6%s has%s %d exp."
            }
            ValueType.TOTAL -> {
                has = system.hasExpTotal(value.toDouble())
                message = "&6%s has%s %d total exp."
            }
        }
        sender.send(message.format(player.name, if (has) "" else " not", value))
    }

    private fun setLevel(system: LevelSystem, command: String) {
        checkArgument(command matches SIGN_NUMBER) { "Level should be a number, can start with + or -." }
        val value = parseValue(command)
        when (Action.of(command)) {
            Action.GIVE -> system.giveLevel(value)
            Action.TAKE -> system.takeLevel(value)
            else -> system.level = value
        }
    }

    private fun setTotalExp(system: LevelSystem, command: String) {
        checkArgument(command matches NUMBER) { "Total experience should be a number." }
        val value = parseValue(command)
        system.totalExp = value.toDouble()
    }

    private fun setExp(system: LevelSystem, command: String) {
        checkArgument(command matches SIGN_NUMBER_PERCENT) {
            "Experience value should be a number, can start with + or - and end with %."
        }
        val percentage = command.endsWith('%')
        val value = parseValue(command).toDouble()
        val action = Action.of(command)
        when {
            percentage -> {
                checkArgument(value <= 100) { "Percentage value can't be greater than 100." }
                checkArgument(action == Action.SET) { "Percentage value not supports + and -." }
                system.fractionalExp = value / 100
            }
            action == Action.GIVE -> system.giveExp(value)
            action == Action.TAKE -> system.takeExp(value)
            else -> system.exp = value
        }
    }

    private inline fun checkArgument(value: Boolean, lazyMessage: () -> String) {
        if (!value) {
            val message = lazyMessage()
            wrongArgument(message, showSyntax = true)
        }
    }

    private fun wrongArgument(message: String, showSyntax: Boolean) {
        throw InvalidCommandArgument(MessageKeys.ERROR_PREFIX, showSyntax, "{message}", message)
    }

    private fun parseValue(command: String): Int = command.replace(Regex("\\D+"), "").toInt()

    @Suppress("UNUSED")
    internal enum class ValueType { LVL, EXP, TOTAL }

    private enum class Action {
        SET, GIVE, TAKE;

        companion object {
            internal fun of(command: String): Action {
                return when (command.first()) {
                    '+' -> GIVE
                    '-' -> TAKE
                    else -> SET
                }
            }
        }
    }
}
