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

import me.robin.battlelevels.api.BattleLevelsAPI;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.mimic.api.system.ExpLevelConverter;

/**
 * Converter for BattleLevels level system.
 */
public class BattleLevelsConverter implements ExpLevelConverter {
    private static BattleLevelsConverter instance;

    private BattleLevelsConverter() {
        // Deny access to constructor from other classes
    }

    static @NotNull BattleLevelsConverter getInstance() {
        if (instance == null) {
            instance = new BattleLevelsConverter();
        }

        return instance;
    }

    @Override
    public double getExpToReachLevel(int level) {
        return BattleLevelsAPI.getNeededFor(level);
    }
}
