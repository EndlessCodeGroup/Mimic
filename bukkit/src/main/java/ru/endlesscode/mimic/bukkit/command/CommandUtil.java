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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandUtil {

    public void send(@NotNull CommandSender target, String... messages) {
        for (String message : messages) {
            target.sendMessage(colored(message));
        }
    }

    @NotNull
    public Player getTarget(@NotNull CommandSender sender, @NotNull String playerName) {
        if (!playerName.isEmpty()) {
            //noinspection deprecation
            Player player = Bukkit.getPlayer(playerName);

            if (player == null) {
                throw new IllegalArgumentException("Error: Player '" + playerName + "' not found");
            }

            return player;
        }

        if (sender instanceof Player) {
            return (Player) sender;
        }

        throw new IllegalArgumentException("Error: You should specify target player");
    }

    @NotNull
    public String msg(@NotNull String text, Object... args) {
        return String.format(text, args);
    }

    @NotNull
    public String colored(@NotNull String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
