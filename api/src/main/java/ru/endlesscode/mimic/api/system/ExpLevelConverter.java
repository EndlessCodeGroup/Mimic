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
 * This interface contains all methods needed to convert levels
 * to exp and vice versa.
 *
 * @author Osip Fatkullin
 * @since 0.1
 */
public abstract class ExpLevelConverter {
    /**
     * Converts experience to full level.
     *
     * @param exp Experience amount
     * @return Amount of full levels
     */
    public int expToFullLevel(double exp) {
        return (int) expToLevel(exp);
    }

    /**
     * Converts experience to level.
     *
     * @param expValue Experience amount
     * @return Level amount
     */
    public double expToLevel(double expValue) {
        if (expValue < 0) {
            return 0;
        }

        double exp = expValue;
        double level = 1;
        double requiredExp;
        for (int i = 1;; i++) {
            requiredExp = getExpToReachNextLevel(i);
            level++;
            exp -= requiredExp;

            if (exp <= 0) {
                level += exp / requiredExp;
                break;
            }
        }

        return level;
    }

    /**
     * Converts level to exp.
     *
     * @param level Player level
     * @return Experience amount to reach given level from 0 exp
     */
    public abstract double levelToExp(int level);

    /**
     * Gets how much experience you need to reach specified level.
     *
     * @param level Current level
     * @return Experience from current to next level
     */
    public abstract double getExpToReachNextLevel(int level);
}
