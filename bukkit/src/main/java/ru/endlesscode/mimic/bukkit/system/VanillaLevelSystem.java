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
import ru.endlesscode.mimic.api.system.registry.Subsystem;
import ru.endlesscode.mimic.api.system.registry.SubsystemPriority;

/** Vanilla experience bar system. */
@Subsystem(priority = SubsystemPriority.LOWEST)
public class VanillaLevelSystem extends BukkitLevelSystem {
    public static final String TAG = "Vanilla Level System";
    public static final Factory<VanillaLevelSystem> FACTORY = new Factory<>(TAG, playerObj -> new VanillaLevelSystem((Player) playerObj));

    private VanillaLevelSystem(@NotNull Player player) {
        super(VanillaConverter.getInstance(), player);
    }

    @Override
    public int getLevel() {
        return getPlayer().getLevel();
    }

    @Override
    public void setLevel(int newLevel) {
        int allowedLevel = Math.max(0, newLevel);

        getPlayer().setLevel(allowedLevel);
    }

    @Override
    public double getExp() {
        int level = getLevel();
        double expToLevel = getConverter().getExpToReachNextLevel(level);

        return expToLevel * getPlayer().getExp();
    }

    @Override
    public void setExp(double newExperience) {
        int level = getLevel();
        double expToNextLevel = getConverter().getExpToReachNextLevel(level);
        double allowedExperience = Math.max(0, newExperience);
        allowedExperience = Math.min(allowedExperience, expToNextLevel);

        getPlayer().setExp((float) (allowedExperience / expToNextLevel));
    }

    public double getFractionalExp() {
        return getPlayer().getExp();
    }

    @Override
    public void setFractionalExp(double fractionalExp) {

        float allowedExp = Math.min(1, (float) fractionalExp);
        allowedExp = Math.max(0, allowedExp);

        getPlayer().setExp(allowedExp);
    }

    @Override
    public double getTotalExpToNextLevel() {
        return getPlayer().getExpToLevel();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @NotNull
    @Override
    public String getName() {
        return TAG;
    }
}
