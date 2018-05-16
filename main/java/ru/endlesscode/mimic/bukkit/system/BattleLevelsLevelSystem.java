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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.mimic.api.system.registry.Metadata;
import ru.endlesscode.mimic.api.system.registry.SystemPriority;

import java.util.UUID;


/**
 * It's implementation of LevelSystem that uses BattleLevels.
 */
@Metadata(
        priority = SystemPriority.NORMAL,
        classes = {"me.robin.battlelevels.api.BattleLevelsAPI"})
public class BattleLevelsLevelSystem extends BukkitLevelSystem {
    public static final String TAG = "BattleLevels";
    public static final Factory FACTORY = new Factory(playerObj -> new BattleLevelsLevelSystem((Player) playerObj), TAG);

    private BattleLevelsLevelSystem(@NotNull Player player) {
        super(BattleLevelsConverter.getInstance(), player);
    }

    @Override
    public int getLevel() {
        return BattleLevelsAPI.getLevel(getPlayerUniqueId());
    }

    @Override
    public void setLevel(int newLevel) {
        int delta = getLevel() - newLevel;

        if (delta < 0) {
            decreaseLevel(Math.abs(delta));
        } else if (delta > 0) {
            increaseLevel(delta);
        }
    }

    @Override
    public void increaseLevel(int lvlAmount) {
        BattleLevelsAPI.addLevel(getPlayerUniqueId(), lvlAmount);
    }

    @Override
    public void decreaseLevel(int lvlAmount) {
        BattleLevelsAPI.removeLevel(getPlayerUniqueId(), lvlAmount);
    }

    @Override
    public double getExp() {
        return BattleLevelsAPI.getScore(getPlayerUniqueId());
    }

    @Override
    public void setExp(double newExperience) {
        double delta = getExp() - newExperience;

        if (delta < 0) {
            takeExp(Math.abs(delta));
        } else if (delta > 0) {
            giveExp(delta);
        }
    }

    @Override
    public void giveExp(double expAmount) {
        BattleLevelsAPI.removeScore(getPlayerUniqueId(), expAmount);
    }

    @Override
    public void takeExp(double expAmount) {
        BattleLevelsAPI.removeScore(getPlayerUniqueId(), expAmount);
    }

    @Override
    public double getExpToNextLevel() {
        return BattleLevelsAPI.getNeededForNext(getPlayerUniqueId());
    }

    @Override
    public double getExpToNextLevelRemaining() {
        return BattleLevelsAPI.getNeededForNextRemaining(getPlayerUniqueId());
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public @NotNull String getName() {
        return TAG;
    }

    private UUID getPlayerUniqueId() {
        return playerRef.get().getUniqueId();
    }
}
