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

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ExpSource;
import com.sucy.skill.api.player.PlayerClass;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.endlesscode.mimic.api.system.registry.Subsystem;
import ru.endlesscode.mimic.api.system.registry.SubsystemPriority;

/**
 * It's implementation of LevelSystem that uses SkillAPI.
 */
@Subsystem(
        priority = SubsystemPriority.NORMAL,
        classes = {"com.sucy.skill.SkillAPI"})
public class SkillApiLevelSystem extends BukkitLevelSystem {
    public static final String TAG = "SkillAPI";
    public static final Factory<SkillApiLevelSystem> FACTORY = new Factory<>(TAG, player -> new SkillApiLevelSystem((Player) player));

    private SkillApiLevelSystem(Player player) {
        super(SkillApiConverter.getInstance(), player);
    }

    @Override
    public void decreaseLevel(int lvlAmount) {
        PlayerClass playerClass = getPlayerClass();

        if (playerClass != null) {
            int newLevel = getLevel() - lvlAmount;
            if (newLevel < 1) {
                newLevel = 1;
            }
            double fractional = getFractionalExp();

            setLevel(newLevel);
            setFractionalExp(fractional);
        }
    }

    @Override
    public void increaseLevel(int lvlAmount) {
        PlayerClass playerClass = getPlayerClass();
        if (playerClass != null) {
            playerClass.giveLevels(lvlAmount);
        }
    }

    @Override
    public int getLevel() {
        PlayerClass playerClass = getPlayerClass();
        return playerClass == null ? 0 : playerClass.getLevel();
    }

    @Override
    public void setLevel(int newLevel) {
        PlayerClass playerClass = getPlayerClass();
        if (playerClass != null) {
            playerClass.setLevel(newLevel);
        }
    }

    @Override
    public void takeExp(double expAmount) {
        PlayerClass playerClass = getPlayerClass();
        if (playerClass != null) {
            double percent = expAmount / playerClass.getRequiredExp();
            playerClass.loseExp(percent);
        }
    }

    @Override
    public void giveExp(double expAmount) {
        PlayerClass playerClass = getPlayerClass();
        if (playerClass != null) {
            playerClass.giveExp(expAmount, ExpSource.SPECIAL);
        }
    }

    @Override
    public double getExp() {
        PlayerClass playerClass = getPlayerClass();
        return playerClass == null ? 0 : playerClass.getExp();
    }

    @Override
    public void setExp(double newExperience) {
        PlayerClass playerClass = getPlayerClass();
        if (playerClass != null) {
            playerClass.setExp(newExperience);
        }
    }

    @Override
    public double getTotalExpToNextLevel() {
        PlayerClass playerClass = getPlayerClass();
        return playerClass == null ? -1 : playerClass.getRequiredExp();
    }

    private @Nullable PlayerClass getPlayerClass() {
        return SkillAPI.getPlayerData(getPlayer()).getMainClass();
    }

    @Override
    public boolean isEnabled() {
        return SkillAPI.isLoaded();
    }

    @NotNull
    @Override
    public String getName() {
        return TAG;
    }
}
