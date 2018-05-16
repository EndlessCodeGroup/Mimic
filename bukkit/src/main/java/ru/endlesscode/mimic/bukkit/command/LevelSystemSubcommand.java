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

package ru.endlesscode.mimic.bukkit.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.mimic.api.system.LevelSystem;
import ru.endlesscode.mimic.api.system.SystemFactory;
import ru.endlesscode.mimic.bukkit.util.Log;

@CommandAlias("%command")
@CommandPermission("%perm")
@Subcommand("level|lvl|l")
public class LevelSystemSubcommand extends BaseCommand {

    private final SystemFactory<LevelSystem> systemFactory;
    private final CommandUtil util;

    public LevelSystemSubcommand(@NotNull SystemFactory<LevelSystem> systemFactory, @NotNull CommandUtil util) {
        this.systemFactory = systemFactory;
        this.util = util;
    }

    @Subcommand("info|i")
    @Description("Show information about player's level system")
    @CommandCompletion("@players")
    public void info(@NotNull CommandSender sender, @Default String player) throws InvalidCommandArgument {
        Player target = util.getTarget(sender, player);
        LevelSystem system = systemFactory.get(target);

        util.send(sender,
                util.msg("&3System: &7%s", system.getName()),
                util.msg("&3Level: &7%.2f", system.getLevel() + system.getFractionalExp()),
                util.msg("&3Exp: &7%.1f &8| &3To next level: %.1f", system.getExp(), system.getExpToNextLevel()),
                util.msg("&3Total exp: &7%.1f", system.getTotalExp())
        );
    }

    @Subcommand("set|s")
    @Description("Change player's level, exp or total exp")
    @CommandCompletion("+|- lvl|exp|total @players")
    public void set(CommandSender sender, String value, @Default("lvl") ValueType type, @Default String player)
            throws InvalidCommandArgument {
        try {
            Player target = util.getTarget(sender, player);
            LevelSystem ls = systemFactory.get(target);

            switch (type) {
                case LVL:
                    setLevel(ls, value);
                    util.send(sender, util.msg("&6New %s's level is %d", target.getName(), ls.getLevel()));
                    break;
                case TOTAL:
                    setTotalExp(ls, value);
                    util.send(sender, util.msg(
                            "&6New %s's total exp is %.1f (%d lvl)", target.getName(), ls.getTotalExp(), ls.getLevel()
                    ));
                    break;
                default:
                    setExp(ls, value);
                    util.send(sender, util.msg(
                            "&6New %s's exp is %.1f (%.2f lvl)", target.getName(), ls.getExp(), ls.getLevel() + ls.getFractionalExp()
                    ));

            }
        } catch (UnsupportedOperationException e) {
            Log.d(e, true);
            wrongArgument(e.getMessage(), false);
        }
    }

    @Subcommand("reach|r")
    @Description("Check that player did reach level")
    @CommandCompletion("@nothing @players")
    public void reach(@NotNull CommandSender sender, int level, @Default String player) throws InvalidCommandArgument {
        Player target = util.getTarget(sender, player);
        boolean reached = systemFactory.get(target).didReachLevel(level);

        util.send(sender, util.msg(
                "&6Player '%s' did%s reach %d lvl.", target.getName(), reached ? "" : " not", level
        ));
    }

    private void setLevel(@NotNull LevelSystem system, @NotNull String command) throws InvalidCommandArgument {
        if (!command.matches("[+\\-]?\\d+")) {
            wrongArgument("Level should be a number, can start with + or -.");
        }

        int value = parseValue(command);
        Action action = Action.parse(command);

        switch (action) {
            case PLUS:
                system.increaseLevel(value);
                break;
            case MINUS:
                system.decreaseLevel(value);
                break;
            default:
                system.setLevel(value);
        }
    }

    private void setTotalExp(@NotNull LevelSystem system, @NotNull String command) throws InvalidCommandArgument {
        if (!command.matches("\\d+")) {
            wrongArgument("Total experience should be a number.");
        }

        int value = parseValue(command);
        system.setTotalExp(value);
    }

    private void setExp(@NotNull LevelSystem system, @NotNull String command) throws InvalidCommandArgument {
        if (!command.matches("[+\\-]?\\d+%?")) {
            wrongArgument("Experience value should be a number, can start with + or - and end with %.");
        }

        boolean percentage = command.endsWith("%");
        int value = parseValue(command);
        Action action = Action.parse(command);

        if (percentage) {
            if (value > 100) {
                wrongArgument("Percentage value can't be greater than 100.");
            }

            if (action == Action.SET) {
                system.setFractionalExp(value / 100.0);
            } else {
                wrongArgument("Percentage value not supports + and -.");
            }

            return;
        }

        switch (action) {
            case PLUS:
                system.giveExp(value);
                break;
            case MINUS:
                system.takeExp(value);
                break;
            default:
                system.setExp(value);
        }
    }

    private void wrongArgument(String message) throws InvalidCommandArgument {
        wrongArgument(message, true);
    }

    private void wrongArgument(String message, boolean showSyntax) throws InvalidCommandArgument {
        throw new InvalidCommandArgument(MessageKeys.ERROR_PREFIX, showSyntax, "{message}", message);
    }

    private int parseValue(String command) {
        return Integer.parseInt(command.replaceAll("[+\\-%]", ""));
    }

    public enum ValueType {
        EXP, TOTAL, LVL
    }

    private enum Action {
        SET, PLUS, MINUS;

        static Action parse(String command) {
            switch (command.charAt(0)) {
                case '+': return PLUS;
                case '-': return MINUS;
                default: return SET;
            }
        }
    }
}
