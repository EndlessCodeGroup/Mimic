/*
 * This file is part of MimicAPI.
 * Copyright (C) 2018 Osip Fatkullin
 * Copyright (C) 2018 EndlessCode Group and contributors
 *
 * MimicAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MimicAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MimicAPI.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.api.system;

import org.jetbrains.annotations.NotNull;
import ru.endlesscode.mimic.api.system.registry.Subsystem;
import ru.endlesscode.mimic.api.system.registry.SubsystemPriority;

/**
 * Basic implementation of LevelSystem independent from player-related object.
 *
 * @author Osip Fatkullin
 * @since 0.1
 */
@Subsystem(priority = SubsystemPriority.LOWEST)
public class BasicLevelSystemImpl implements LevelSystem {
    public static final String TAG = "Basic Level System";
    public static final LevelSystem.Factory FACTORY = new LevelSystem.Factory(TAG, arg -> new BasicLevelSystemImpl());

    private final ExpLevelConverter converter;

    private int level;
    private double exp;

    /**
     * Constructor that initialize basic converter.
     */
    public BasicLevelSystemImpl() {
        this.converter = new BasicConverterImp();
    }

    @NotNull
    @Override
    public ExpLevelConverter getConverter() {
        return converter;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public @NotNull Object getHandler() {
        return this;
    }

    @Override
    public String getName() {
        return TAG;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public void setLevel(int newLevel) {
        this.level = Math.max(0, newLevel);
    }

    @Override
    public double getExp() {
        return this.exp;
    }

    @Override
    public void setExp(double newExperience) {
        double allowedExp = Math.max(0, newExperience);
        allowedExp = Math.min(getConverter().getExpToReachNextLevel(0) - 1, allowedExp);

        this.exp = allowedExp;
    }

    @Override
    public double getTotalExpToNextLevel() {
        return getConverter().getExpToReachNextLevel(0) - this.exp;
    }
}
