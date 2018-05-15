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
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Split;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.mimic.api.system.ClassSystem;
import ru.endlesscode.mimic.api.system.SystemFactory;

import java.util.Arrays;

@CommandAlias("%command")
@CommandPermission("%perm")
@Subcommand("class|c")
public class ClassSystemSubcommand extends BaseCommand {

    private final SystemFactory<ClassSystem> systemFactory;
    private final CommandUtil util;

    public ClassSystemSubcommand(SystemFactory<ClassSystem> systemFactory, CommandUtil util) {
        this.systemFactory = systemFactory;
        this.util = util;
    }

    @Subcommand("info|i")
    @Description("Show information about player's class system")
    @CommandCompletion("@players")
    public void info(@NotNull CommandSender sender, @Default String player) throws InvalidCommandArgument {
        Player target = util.getTarget(sender, player);
        ClassSystem system = systemFactory.get(target);

        util.send(sender,
                util.msg("&3System: &7%s", system.getName()),
                util.msg("&3Classes: &7%s", system.getClasses()),
                util.msg("&3Primary: &7%s", system.getPrimaryClass())
        );
    }

    @Subcommand("has|h")
    @Description("Check that player has given classes")
    @CommandCompletion("@nothing one|all @players")
    public void has(CommandSender sender, @Split String[] classes, @Default("all") Mode mode, @Default String player)
            throws InvalidCommandArgument {
        Player target = util.getTarget(sender, player);
        ClassSystem system = systemFactory.get(target);

        boolean has;
        switch (mode) {
            case ALL:
                has = system.hasAllRequiredClasses(Arrays.asList(classes));
                break;
            default:
                has = system.hasOneOfRequiredClasses(Arrays.asList(classes));
        }

        util.send(sender, util.msg("&6Player '%s' has%s given classes.", target.getName(), has ? "" : " not"));
    }

    public enum Mode {
        ONE, ALL
    }
}
