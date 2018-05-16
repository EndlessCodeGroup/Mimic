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
import ru.endlesscode.mimic.api.system.registry.Metadata;
import ru.endlesscode.mimic.api.system.registry.SystemPriority;

/**
 * Vanilla experience bar system.
 */
@Metadata(priority = SystemPriority.LOWEST)
public class VanillaLevelSystem extends BukkitLevelSystem {
    public static final String TAG = "Vanilla Level System";
    public static final Factory FACTORY = new Factory(playerObj -> new VanillaLevelSystem((Player) playerObj), TAG);

    private VanillaLevelSystem(@NotNull Player player) {
        super(VanillaConverter.getInstance(), player);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public int getLevel() {
        Player player = playerRef.get();
        return player.getLevel();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setLevel(int newLevel) {
        int allowedLevel = Math.max(0, newLevel);

        Player player = playerRef.get();
        player.setLevel(allowedLevel);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public int getExp() {
        int level = getLevel();
        int expToLevel = converter.getExpToReachNextLevel(level);

        Player player = playerRef.get();
        return (int) (expToLevel * player.getExp());
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setExp(int newExperience) {
        int level = getLevel();
        int expToNextLevel = converter.getExpToReachNextLevel(level);
        float allowedExperience = Math.max(0, newExperience);
        allowedExperience = Math.min(allowedExperience, expToNextLevel);

        Player player = playerRef.get();
        player.setExp(allowedExperience / expToNextLevel);
    }

    /**
     * {@inheritDoc}.
     */
    public double getFractionalExp() {
        Player player = playerRef.get();
        return player.getExp();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setFractionalExp(double fractionalExp) {
        Player player = playerRef.get();

        float allowedExp = Math.min(1, (float) fractionalExp);
        allowedExp = Math.max(0, allowedExp);

        player.setExp(allowedExp);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public int getExpToNextLevel() {
        Player player = playerRef.get();
        return player.getExpToLevel() - getExp();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * {@inheritDoc}.
     */
    @NotNull
    @Override
    public String getName() {
        return TAG;
    }
}
