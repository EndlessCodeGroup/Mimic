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

/**
 * Basic implementation of ExpLevelConverter.
 *
 * <p>For each level needed 10XP.</p>
 *
 * @author Osip Fatkullin
 * @since 0.1
 */
public class BasicConverterImp extends ExpLevelConverter {
    @Override
    public int levelToExp(int level) {
        return level * 10;
    }

    @Override
    public double expToLevel(int exp) {
        return exp / 10.;
    }

    @Override
    public int getExpToReachNextLevel(int level) {
        return 10;
    }
}
