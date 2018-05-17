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

package ru.endlesscode.mimic.api.system

import java.util.function.Function

/**
 * System that provides methods to work with players level systems.
 * You can check or change values of level and experience.
 *
 * Before implementing run an eye over all default method implementations
 * and override all methods that works not properly for your case.
 *
 * @author Osip Fatkullin
 * @since 0.1
 */
interface LevelSystem : PlayerSystem {

    /**
     * Gives assigned converter.
     *
     * @implSpec
     * Must return not null object.
     *
     * @return The converter
     */
    val converter: ExpLevelConverter

    /**
     * Current level of player.
     *
     * @apiNote
     * Level can not be lesser than zero.
     *
     * @throws IllegalStateException If player-related object not exists
     */
    var level: Int


    /**
     * Player's total experience points.
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    var totalExp: Double
        get() {
            val levelExp = this.converter.levelToExp(this.level)
            return levelExp + this.exp
        }
        set(newTotalExperience) {
            val allowedTotalExperience = Math.max(0.0, newTotalExperience)

            val level = this.converter.expToLevel(allowedTotalExperience)
            val fullLevel = this.converter.expToFullLevel(allowedTotalExperience)
            val expToNextLevel = this.converter.getExpToReachNextLevel(fullLevel)
            val experiencePercent = level - fullLevel

            this.level = fullLevel
            this.exp = expToNextLevel * experiencePercent
        }

    /**
     * Current fractional XP.
     *
     * @apiNote
     * This is a percentage value. 0 is "no progress" and 1 is "next level".
     *
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    var fractionalExp: Double
        get() {
            val exp = this.exp

            if (exp == 0.0) {
                return 0.0
            }

            val level = this.level
            return exp / converter.getExpToReachNextLevel(level)
        }
        set(fractionalExp) {
            val level = level
            this.exp = converter.getExpToReachNextLevel(level) * fractionalExp
        }

    /**
     * Player's current level experience points.
     *
     * @apiNote
     * This field contains experience on current level, to get total player
     * experience use [totalExp].
     * Be careful with this field! To change experience value better to use
     * [giveExp] and [takeExp]. To set total player experience use [totalExp].
     * New experience value shouldn't be less than 0 and bigger than maximal
     * possible XP on current level.
     *
     * @implNote
     * You should add argument value validation to your implementation because
     * new value may be bigger than maximal possible experience on current level,
     * and you must trim it to the limit.
     *
     * @return Current level experience points or 0 if player has no exp
     * @throws IllegalStateException If player-related object not exists
     */
    var exp: Double

    /**
     * Total amount of experience required for the player to reach next level.
     *
     * @return Experience required to level up or -1 if level-up is impossible
     * @throws IllegalStateException If player-related object not exists
     */
    val expToNextLevel: Double

    /**
     * Remaining experience for the player to reach next level.
     *
     * @return Experience required to level up or -1 if level-up is impossible
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    val expToNextLevelRemaining: Double get() = expToNextLevel - exp

    /**
     * Decreases the player level by a specified amount.
     *
     * @apiNote
     * Never use negative amount to increase player level, use [increaseLevel] instead.
     *
     * @param lvlAmount Amount of levels to take away
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    fun decreaseLevel(lvlAmount: Int) {
        val currentLevel = this.level
        val allowedAmount = Math.min(lvlAmount, currentLevel)
        this.increaseLevel(-allowedAmount)
    }

    /**
     * Increases the player level by a specified amount.
     *
     * @apiNote
     * Never use negative amount to decrease player level, use [decreaseLevel] instead.
     *
     * @param lvlAmount Amount of additional levels
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    fun increaseLevel(lvlAmount: Int) {
        val currentLevel = this.level
        this.level = currentLevel + lvlAmount
    }

    /**
     * Checks player reached required experience level.
     *
     * @apiNote
     * Required level shouldn't be less than 0.
     *
     * @param requiredLevel Required experience level
     * @return true if player player did reach required level
     */
    @JvmDefault
    fun didReachLevel(requiredLevel: Int): Boolean {
        require(requiredLevel >= 0) { "Required level can't be less than 0." }
        return requiredLevel <= this.level
    }

    /**
     * Takes away player the amount of experience specified.
     *
     * @param expAmount Exp amount to take away
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    fun takeExp(expAmount: Double) {
        val currentTotalExp = this.totalExp
        val allowedAmount = Math.min(expAmount, currentTotalExp)
        this.giveExp(-allowedAmount)
    }

    /**
     * Gives player the amount of experience specified.
     *
     * @apiNote
     * This method relates to total experience, which means that it can
     * change both level and experience.
     *
     * @param expAmount Exp amount to give
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    fun giveExp(expAmount: Double) {
        val totalExp = this.totalExp
        this.totalExp = totalExp + expAmount
    }

    /**
     * Checks player has required experience on current level.
     *
     * @param requiredExp Required experience amount
     * @return true if player player has required experience
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    fun hasExp(requiredExp: Double): Boolean {
        require(requiredExp >= 0) { "Required exp can't be less than 0." }
        return requiredExp <= this.exp
    }

    /**
     * Checks player has required total experience.
     *
     * @param requiredExp Required total experience amount
     * @return true if player player has required total experience
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    fun hasExpTotal(requiredExp: Double): Boolean {
        require(requiredExp >= 0) { "Required total exp can't be less than 0." }
        return requiredExp <= this.totalExp
    }

    /**
     * Factory of level systems.
     */
    class Factory(constructor: Function<Any, out LevelSystem>, tag: String) : SystemFactory<LevelSystem>(constructor, tag)
}
