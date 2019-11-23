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

/**
 * System that provides methods to work with players level systems.
 * You can check or change values of level and experience.
 *
 * Before implementing run an eye over all default method implementations
 * and override all methods that works not properly for your case.
 */
interface LevelSystem : PlayerSystem {

    /** Assigned converter. */
    val converter: ExpLevelConverter

    /**
     * Current level of the player.
     *
     * Level can not be lesser than zero.
     *
     * @throws IllegalStateException If player-related object not exists
     */
    var level: Int

    /**
     * Player's total experience points.
     *
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    var totalExp: Double
        get() {
            val levelExp = this.converter.levelToExp(this.level)
            return levelExp + this.exp
        }
        set(value) {
            val allowedTotalExperience = value.coerceAtLeast(0.0)

            val level = this.converter.expToLevel(allowedTotalExperience)
            val fullLevel = this.converter.expToFullLevel(allowedTotalExperience)
            val expToNextLevel = this.converter.getExpToReachNextLevel(fullLevel)
            val levelPercent = level - fullLevel

            this.level = fullLevel
            this.exp = expToNextLevel * levelPercent
        }

    /**
     * Current fractional XP.
     *
     * This is a percentage value. 0 means "no progress" and 1 is "next level".
     *
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    var fractionalExp: Double
        get() {
            val expToNextLevel = converter.getExpToReachNextLevel(this.level)
            val exp = this.exp.coerceAtMost(expToNextLevel - 1)
            return if (exp <= 0.0) 0.0 else exp / expToNextLevel
        }
        set(value) {
            val allowedExp = value.coerceAtLeast(0.0)
            if (value < 1.0) {
                this.exp = converter.getExpToReachNextLevel(this.level) * allowedExp
            } else {
                this.giveLevel(1)
                this.exp = 0.0
            }
        }

    /**
     * Player's current level experience points.
     *
     * This field contains experience on current level, to get total player
     * experience use [totalExp].
     * Be careful with this field! To change experience value better to use
     * [giveExp] and [takeExp]. To set total player experience use [totalExp].
     * New experience value shouldn't be less than 0 and bigger than maximal
     * possible XP on current level.
     *
     * For implementation:
     * You should add argument value validation to your implementation because
     * new value can be bigger than maximal possible experience on current level,
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
    val totalExpToNextLevel: Double

    /**
     * Remaining experience for the player to reach next level.
     *
     * @return Experience required to level up or -1 if level-up is impossible
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    val expToNextLevel: Double
        get() = totalExpToNextLevel - exp

    /**
     * Decreases the player level by a specified amount.
     *
     * Never use negative amount to increase player level, use [giveLevel] instead.
     *
     * @param lvlAmount Amount of levels to take away
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    fun takeLevel(lvlAmount: Int) {
        val currentLevel = this.level
        this.level = currentLevel - lvlAmount.coerceAtMost(currentLevel)
    }

    /**
     * Increases the player level by a specified amount.
     *
     * Never use negative amount to decrease player level, use [takeLevel] instead.
     *
     * @param lvlAmount Amount of additional levels
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    fun giveLevel(lvlAmount: Int) {
        this.level += lvlAmount
    }

    /**
     * Checks player reached required experience level.
     *
     * @param requiredLevel Required experience level. Negative value will be interpreted as 0.
     * @return true if player player did reach required level
     */
    @JvmDefault
    fun didReachLevel(requiredLevel: Int): Boolean = requiredLevel.coerceAtLeast(0) <= this.level

    /**
     * Takes away player the amount of experience specified.
     *
     * This method affects to total experience, which means that it can
     * change both level and experience.
     * Never use negative amount to increase exp, use [giveExp] instead.
     *
     * @param expAmount Exp amount to take away
     * @throws IllegalStateException If player-related object not exists
     * @see giveExp
     */
    @JvmDefault
    fun takeExp(expAmount: Double) {
        val currentTotalExp = this.totalExp
        this.totalExp = currentTotalExp - expAmount.coerceAtMost(currentTotalExp)
    }

    /**
     * Gives player the amount of experience specified.
     *
     * This method affects to total experience, which means that it can
     * change both level and experience.
     * Never use negative amount to decrease exp, use [takeExp] instead.
     *
     * @param expAmount Exp amount to give
     * @throws IllegalStateException If player-related object not exists
     * @see takeExp
     */
    @JvmDefault
    fun giveExp(expAmount: Double) {
        totalExp += expAmount
    }

    /**
     * Checks player has required experience on current level.
     *
     * @param requiredExp Required experience amount. Negative value will be interpreted as 0
     * @return true if player player has required experience
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    fun hasExp(requiredExp: Double): Boolean = requiredExp.coerceAtLeast(0.0) <= this.exp

    /**
     * Checks player has required total experience.
     * @param requiredExp Required total experience amount. Negative value will be interpreted as 0.
     * @return true if player player has required total experience
     * @throws IllegalStateException If player-related object not exists
     */
    @JvmDefault
    fun hasExpTotal(requiredExp: Double): Boolean = requiredExp.coerceAtLeast(0.0) <= this.totalExp

    /** Factory of level systems. */
    open class Factory<SubsystemT : LevelSystem>(tag: String, constructor: (Any) -> SubsystemT) :
        SystemFactory<SubsystemT>(tag, constructor)
}
