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

import com.sucy.skill.*;
import com.sucy.skill.api.player.*;
import org.bukkit.entity.*;
import org.jetbrains.annotations.*;
import ru.endlesscode.mimic.api.system.registry.*;

import java.util.*;

/**
 * It's implementation of ClassSystem that uses SkillAPI.
 */
@Metadata(
        priority = SystemPriority.NORMAL,
        classes = {"com.sucy.skill.SkillAPI"})
public class SkillApiClassSystem extends BukkitClassSystem {
    public static final Factory FACTORY = new Factory(playerArg -> new SkillApiClassSystem((Player) playerArg));

    private SkillApiClassSystem(@NotNull Player player) {
        super(player);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public @NotNull List<String> getClasses() {
        PlayerData playerData = getPlayerData();
        Collection<PlayerClass> classes = playerData.getClasses();

        List<String> classNames = new ArrayList<>(classes.size());
        for (PlayerClass playerClass : classes) {
            classNames.add(playerClass.getData().getName());
        }

        return classNames;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public @NotNull String getPrimaryClass() {
        PlayerData playerData = getPlayerData();
        PlayerClass playerClass = playerData.getMainClass();

        return playerClass == null ? "" : playerClass.getData().getName();
    }

    private PlayerData getPlayerData() {
        Player player = playerRef.get();
        return SkillAPI.getPlayerData(player);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public boolean isEnabled() {
        return SkillAPI.isLoaded();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public String getName() {
        return "SkillAPI";
    }
}
