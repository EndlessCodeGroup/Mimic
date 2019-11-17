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

import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.mimic.api.system.registry.Subsystem;
import ru.endlesscode.mimic.api.system.registry.SubsystemPriority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * It's implementation of ClassSystem that uses SkillAPI.
 */
@Subsystem(
        priority = SubsystemPriority.NORMAL,
        classes = {"com.sucy.skill.SkillAPI"})
public class SkillApiClassSystem extends BukkitClassSystem {
    public static final String TAG = "SkillAPI";
    public static final Factory<SkillApiClassSystem> FACTORY = new Factory<>(TAG, playerArg -> new SkillApiClassSystem((Player) playerArg));

    private final SkillApiWrapper skillApi;

    private SkillApiClassSystem(@NotNull Player player) {
        this(player, new SkillApiWrapper());
    }

    SkillApiClassSystem(@NotNull Player player, SkillApiWrapper skillApi) {
        super(player);
        this.skillApi = skillApi;
    }

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

    @Override
    public @Nullable String getPrimaryClass() {
        PlayerData playerData = getPlayerData();
        PlayerClass playerClass = playerData.getMainClass();

        return playerClass == null ? null : playerClass.getData().getName();
    }

    private PlayerData getPlayerData() {
        return skillApi.getPlayerData(getPlayer());
    }

    @Override
    public boolean isEnabled() {
        return skillApi.isLoaded();
    }

    @NotNull
    @Override
    public String getName() {
        return TAG;
    }
}
