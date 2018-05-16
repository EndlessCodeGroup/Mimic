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

import java.util.function.Function;

/**
 * System that provides methods to work with players level systems.
 * You can check or change values of level and experience.
 *
 * <p>Before implementing run an eye over all default method implementations
 * and override all methods that works not properly for your case.</p>
 *
 * @author Osip Fatkullin
 * @since 0.1
 */
public abstract class LevelSystem implements PlayerSystem {
    protected final ExpLevelConverter converter;

    /**
     * Constructor that initialize converter.
     *
     * @param converter Converter
     */
    protected LevelSystem(@NotNull ExpLevelConverter converter) {
        this.converter = converter;
    }

    /**
     * Gives assigned converter.
     *
     * @implSpec
     * Must return not null object.
     *
     * @return The converter
     */
    @NotNull
    public ExpLevelConverter getConverter() {
        return this.converter;
    }

    /**
     * Decreases the player level by a specified amount.
     *
     * @apiNote
     * Never use negative amount to increase player level, use
     * {@link #increaseLevel(int)} instead.
     *
     * @param lvlAmount Amount of levels to take away
     */
    public void decreaseLevel(int lvlAmount) {
        int currentLevel = this.getLevel();
        int allowedAmount = Math.min(lvlAmount, currentLevel);
        this.increaseLevel(-allowedAmount);
    }

    /**
     * Increases the player level by a specified amount.
     *
     * @apiNote
     * Never use negative amount to decrease player level, use
     * {@link #decreaseLevel(int)} instead.
     *
     * @param lvlAmount Amount of additional levels
     */
    public void increaseLevel(int lvlAmount) {
        int currentLevel = this.getLevel();
        this.setLevel(currentLevel + lvlAmount);
    }

    /**
     * Checks player reached required experience level.
     *
     * @apiNote
     * Required level shouldn't be less than 0.
     *
     * @param requiredLevel Required experience level
     * @return {@code true} if player player did reach required level
     */
    public boolean didReachLevel(int requiredLevel) {
        return requiredLevel <= this.getLevel();
    }

    /**
     * Gets current experience level of player.
     *
     * @return Current experience level or 0 if player has no level
     * @throws IllegalStateException If player-related object not exists
     */
    public abstract int getLevel();

    /**
     * Sets current experience level for player.
     *
     * @apiNote
     * Argument can not be lesser than zero.
     *
     * @param newLevel New experience level
     * @throws IllegalStateException If player-related object not exists
     */
    public abstract void setLevel(int newLevel);

    /**
     * Takes away player the amount of experience specified.
     *
     * @param expAmount Exp amount to take away
     */
    public void takeExp(double expAmount) {
        double currentTotalExp = this.getTotalExp();
        double allowedAmount = Math.min(expAmount, currentTotalExp);
        this.giveExp(-allowedAmount);
    }

    /**
     * Gives player the amount of experience specified.
     *
     * @apiNote
     * This method relates to total experience, which means that it can
     * change both level and experience.
     *
     * @param expAmount Exp amount to give
     */
    public void giveExp(double expAmount) {
        double totalExp = this.getTotalExp();
        this.setTotalExp(totalExp + expAmount);
    }

    /**
     * Gets player's total experience points.
     *
     * @return Total experience points or 0 if player has no exp
     */
    public double getTotalExp() {
        double levelExp = this.converter.levelToExp(this.getLevel());
        return levelExp + this.getExp();
    }

    /**
     * Sets player's total experience points.
     *
     * @param newTotalExperience New total experience
     */
    public void setTotalExp(double newTotalExperience) {
        double allowedTotalExperience = Math.max(0, newTotalExperience);

        double level = this.converter.expToLevel(allowedTotalExperience);
        int fullLevel = this.converter.expToFullLevel(allowedTotalExperience);
        double expToNextLevel = this.converter.getExpToReachNextLevel(fullLevel);
        double experiencePercent = level - fullLevel;

        this.setLevel(fullLevel);
        this.setExp(expToNextLevel * experiencePercent);
    }

    /**
     * Checks player has required total experience.
     *
     * @param requiredExp Required total experience amount
     * @return {@code true} if player player has required total experience
     */
    public boolean hasExp(double requiredExp) {
        return requiredExp <= this.getTotalExp();
    }

    /**
     * Gets player's current fractional XP.
     *
     * @apiNote
     * This is a percentage value. 0 is "no progress" and 1 is "next level".
     *
     * @return Current fractional XP.
     */
    public double getFractionalExp() {
        double exp = this.getExp();

        if (exp == 0) {
            return 0;
        }

        int level = this.getLevel();
        return exp / converter.getExpToReachNextLevel(level);
    }

    /**
     * Sets player's current fractional XP.
     *
     * @apiNote
     * This is a percentage value. 0 is "no progress" and 1 is "next level".
     *
     * @param fractionalExp Fractional XP value between 0 ans 1.
     */
    public void setFractionalExp(double fractionalExp) {
        int level = getLevel();
        this.setExp(converter.getExpToReachNextLevel(level) * fractionalExp);
    }

    /**
     * Gets player's current level experience points.
     *
     * @apiNote
     * This method returns experience on current level, to get total player
     * experience use {@link #getTotalExp()}
     *
     * @return Current level experience points or 0 if player has no exp
     * @throws IllegalStateException If player-related object not exists
     */
    public abstract double getExp();

    /**
     * Sets player's current level experience points.
     *
     * @apiNote
     * Be careful with this method! To change experience value better to use
     * {@link #giveExp(double)} and {@link #takeExp(double)}. This method changes
     * experience on current level, to set total player experience use
     * {@link #setTotalExp(double)}. New experience value shouldn't be less than 0
     * and bigger than maximal possible XP on current level.
     *
     * @implNote
     * You should add argument value validation to your implementation because
     * new value may be bigger than maximal possible experience on current level,
     * and you must trim it to the limit.
     *
     * @param newExperience New level experience points
     * @throws IllegalStateException If player-related object not exists
     */
    public abstract void setExp(double newExperience);

    /**
     * Get the total amount of experience required for the player to reach level.
     *
     * @return Experience required to level up or -1 if level-up is impossible
     * @throws IllegalStateException If player-related object not exists
     */
    public abstract double getExpToNextLevel();

    /**
     * Get remaining experience for the player to reach level.
     *
     * @return Experience required to level up or -1 if level-up is impossible
     * @throws IllegalStateException If player-related object not exists
     */
    public double getExpToNextLevelRemaining() {
        return getExpToNextLevel() - getExp();
    }

    /**
     * Factory of level systems.
     */
    public static class Factory extends SystemFactory<LevelSystem> {
        public Factory(Function<Object, ? extends LevelSystem> constructor, String tag) {
            super(constructor, tag);
        }
    }
}
