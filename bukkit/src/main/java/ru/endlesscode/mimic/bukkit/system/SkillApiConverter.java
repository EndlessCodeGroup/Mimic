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

import com.sucy.skill.data.Settings;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.mimic.api.system.ExpLevelConverter;

/**
 * Converter for SkillAPI level system.
 */
public class SkillApiConverter implements ExpLevelConverter {
    private static SkillApiConverter instance;
    private final Settings settings;

    private SkillApiConverter(@NotNull SkillApiWrapper skillApi) {
        this.settings = skillApi.getSettings();
    }

    static @NotNull SkillApiConverter getInstance(SkillApiWrapper skillApi) {
        if (instance == null) {
            instance = new SkillApiConverter(skillApi);
        }

        return instance;
    }

    @Override
    public double getExpToReachLevel(int level) {
        return getExpToReachNextLevel(level - 1);
    }

    @Override
    public double getExpToReachNextLevel(int level) {
        return level <= 0 ? -1 : settings.getRequiredExp(level);
    }
}
