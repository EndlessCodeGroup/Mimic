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

package ru.endlesscode.mimic.bukkit.system;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HeroesClassSystem extends BukkitClassSystem {
    /**
     * Constructor that initialize player.
     *
     * @param player The player
     */
    HeroesClassSystem(@NotNull Player player) {
        super(player);
    }

    @Override
    public @NotNull List<String> getClasses() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public @NotNull String getName() {
        return null;
    }
}
