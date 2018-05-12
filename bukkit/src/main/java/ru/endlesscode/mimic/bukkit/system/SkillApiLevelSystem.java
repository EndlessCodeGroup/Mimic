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
import ru.endlesscode.mimic.api.system.registry.Metadata;
import ru.endlesscode.mimic.api.system.registry.SystemPriority;

/**
 * It's implementation of LevelSystem that uses SkillAPI.
 */
@Metadata(
        priority = SystemPriority.NORMAL,
        classes = {"com.sucy.skill.SkillAPI"})
public class SkillApiLevelSystem extends BukkitLevelSystem {
    public static final Factory FACTORY = new Factory(player -> new SkillApiLevelSystem((Player) player));

    private SkillApiLevelSystem(Player player) {
        super(SkillApiConverter.getInstance(), player);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void decreaseLevel(int lvlAmount) {
        throw new UnsupportedOperationException("Level decrease not supported by Skill API.");
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void increaseLevel(int lvlAmount) {
        PlayerClass playerClass = getPlayerClass();
        if (playerClass != null) {
            playerClass.giveLevels(lvlAmount);
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public int getLevel() {
        PlayerClass playerClass = getPlayerClass();
        return playerClass == null ? 0 : playerClass.getLevel();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setLevel(int newLevel) {
        PlayerClass playerClass = getPlayerClass();
        if (playerClass != null) {
            playerClass.setLevel(newLevel);
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void takeExp(int expAmount) {
        PlayerClass playerClass = getPlayerClass();
        if (playerClass != null) {
            double percent = (double) expAmount / playerClass.getRequiredExp();
            playerClass.loseExp(percent);
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void giveExp(int expAmount) {
        PlayerClass playerClass = getPlayerClass();
        if (playerClass != null) {
            playerClass.giveExp(expAmount, ExpSource.SPECIAL);
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public int getExp() {
        PlayerClass playerClass = getPlayerClass();
        return playerClass == null ? 0 : (int) playerClass.getExp();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setExp(int newExperience) {
        PlayerClass playerClass = getPlayerClass();
        if (playerClass != null) {
            playerClass.setExp(newExperience);
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public int getExpToNextLevel() {
        PlayerClass playerClass = getPlayerClass();
        return playerClass == null ? -1 : playerClass.getRequiredExp() - getExp();
    }

    private PlayerClass getPlayerClass() {
        Player player = playerRef.get();
        return SkillAPI.getPlayerData(player).getMainClass();
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
    @NotNull
    @Override
    public String getName() {
        return "SkillAPI";
    }
}
