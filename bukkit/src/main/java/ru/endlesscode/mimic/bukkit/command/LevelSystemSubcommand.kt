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

import co.aikar.commands.BaseCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.MessageKeys
import co.aikar.commands.annotation.*
import org.bukkit.command.CommandSender
import ru.endlesscode.mimic.api.system.LevelSystem
import ru.endlesscode.mimic.api.system.SystemFactory
import ru.endlesscode.mimic.bukkit.util.Log

@CommandAlias("%command")
@CommandPermission("%perm")
@Subcommand("level|lvl|l|experience|exp|xp")
internal class LevelSystemSubcommand(
    private val systemFactory: SystemFactory<LevelSystem>,
    private val util: CommandUtil
) : BaseCommand() {

    companion object {
        private val NUMBER get() = Regex("\\d+")
        private val SIGN_NUMBER get() = Regex("[-+]?\\d+")
        private val SIGN_NUMBER_PERCENT get() = Regex("[-+]?\\d+%?")
    }

    @Subcommand("info|i")
    @Description("Show information about player's level system")
    @Syntax("[player]")
    @CommandCompletion("@players")
    fun info(sender: CommandSender, @Default player: String) {
        val target = util.getTarget(sender, player)
        val system = systemFactory.get(target)
        util.send(
            sender,
            util.msg("&3System: &7%s", system.name),
            util.msg("&3Level: &7%.2f", system.level + system.fractionalExp),
            util.msg("&3Exp: &7%.1f &8| &3To next level: &7%.1f", system.exp, system.expToNextLevel),
            util.msg("&3Total exp: &7%.1f", system.totalExp)
        )
    }

    @Subcommand("set|s")
    @Description("Change player's level, exp or total exp")
    @Syntax("<value> [lvl|exp|total] [player]")
    @CommandCompletion("+|- lvl|exp|total @players")
    fun set(
        sender: CommandSender,
        value: String,
        @Default("exp") type: ValueType,
        @Default player: String
    ) {
        try {
            val target = util.getTarget(sender, player)
            val ls = systemFactory.get(target)
            when (type) {
                ValueType.LVL -> {
                    setLevel(ls, value)
                    util.send(sender, util.msg("&6New %s's level is %d", target.name, ls.level))
                }
                ValueType.TOTAL -> {
                    setTotalExp(ls, value)
                    util.send(
                        sender,
                        util.msg("&6New %s's total exp is %.1f (%d lvl)", target.name, ls.totalExp, ls.level)
                    )
                }
                else -> {
                    setExp(ls, value)
                    util.send(
                        sender,
                        util.msg("&6New %s's exp is %.1f (%.2f lvl)", target.name, ls.exp, ls.level + ls.fractionalExp)
                    )
                }
            }
        } catch (e: UnsupportedOperationException) {
            Log.d(e, quiet = true)
            wrongArgument(e.message.toString(), showSyntax = false)
        }
    }

    @Subcommand("reach|r")
    @Description("Check that player did reach level")
    @Syntax("<level> [player]")
    @CommandCompletion("@nothing @players")
    fun reach(
        sender: CommandSender,
        level: Int,
        @Default player: String
    ) {
        val target = util.getTarget(sender, player)
        val reached = systemFactory.get(target).didReachLevel(level)
        util.send(
            sender,
            util.msg("&6Player '%s' did%s reach %d lvl.", target.name, if (reached) "" else " not", level)
        )
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
    internal enum class ValueType { EXP, TOTAL, LVL }

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
