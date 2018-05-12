/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2017 EndlessCode Group and contributors
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

import org.jetbrains.annotations.*;
import ru.endlesscode.mimic.api.system.*;

/**
 * Converter for vanilla experience system.
 *
 * @see <a href="http://minecraft.gamepedia.com/Experience#Leveling_up">Minecraft Wiki: Experience - Leveling Up</a>
 */
public class VanillaConverter extends ExpLevelConverter {
    private static VanillaConverter instance;

    private VanillaConverter() {
        // Deny access to constructor from other classes
    }

    static @NotNull VanillaConverter getInstance() {
        if (instance == null) {
            instance = new VanillaConverter();
        }

        return instance;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public double expToLevel(int exp) {
        double level = 0;
        if (exp >= 1628) {
            level = (Math.sqrt(72 * exp - 54215) + 325) / 18;
        } else if (exp >= 394) {
            level = Math.sqrt(40 * exp - 7839) / 10 + 8.1;
        } else if (exp > 0) {
            level = Math.sqrt(exp + 9) - 3;
        }

        return level;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public int levelToExp(int level) {
        int exp = 0;
        if (level >= 32) {
            exp = (int) (4.5 * level * level - 162.5 * level + 2220);
        } else if (level >= 17) {
            exp = (int) (2.5 * level * level - 40.5 * level + 360);
        } else if (level > 0) {
            exp = level * level + 6 * level;
        }

        return exp;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public int getExpToReachNextLevel(int level) {
        int exp = -1;
        if (level >= 31) {
            exp = 9 * level - 158;
        } else if (level >= 16) {
            exp = 5 * level - 38;
        } else if (level >= 0) {
            exp = 2 * level + 7;
        }

        return exp;
    }
}
