/*
 * This file is part of Mimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
 *
 * Mimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Mimic.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.level

/** Entity that contains methods needed to convert levels to exp and vice versa. */
public interface ExpLevelConverter {

    /** Converts [exp] to full level, dropping fractional part. */
    public fun expToFullLevel(exp: Double): Int = expToLevel(exp).toInt()

    /** Converts [exp] to level, with fractional part. */
    public fun expToLevel(exp: Double): Double {
        if (exp < 0) return 0.0

        var remainingExp = exp
        var level = 0.0
        var requiredExp: Double
        var i = 0
        while (true) {
            level++

            requiredExp = getExpToReachNextLevel(i)
            if (requiredExp == -1.0) {
                if (i == 0) {
                    i++
                    continue
                }

                break
            }

            remainingExp -= requiredExp

            if (remainingExp <= 0) {
                level += remainingExp / requiredExp
                break
            }
            i++
        }

        return level
    }

    /** Returns experience amount to reach given [level] from 0 exp. */
    public fun levelToExp(level: Int): Double {
        var exp = 0.0
        for (i in 0 until level) {
            val expToReachNext = getExpToReachNextLevel(i)
            if (expToReachNext != -1.0) {
                exp += expToReachNext
            }
        }

        return exp
    }

    /**
     * Returns how much experience you need to reach next after specified [level] or -1 if
     * level can't be reached.
     */
    public fun getExpToReachNextLevel(level: Int): Double = getExpToReachLevel(level + 1)

    /**
     * Returns how much experience you need to reach specified [level] or -1 if
     * level can't be reached.
     */
    public fun getExpToReachLevel(level: Int): Double
}
