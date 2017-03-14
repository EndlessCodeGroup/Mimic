/*
 * This file is part of Mimic.
 * Copyright (C) 2017 Osip Fatkullin
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

package ru.endlesscode.mimic.levels;

import ru.endlesscode.mimic.PlayerSystem;

/**
 * System that provides methods to work with players level systems.
 * You can check or change values of level and experience.
 *
 * @author Osip Fatkullin
 * @since 1.0
 */
public interface LevelSystem extends PlayerSystem {
    /**
     * Gets current experience level of player
     *
     * @return Current experience level
     */
    public int getLevel();

    /**
     * Sets current experience level for player
     *
     * @param newLevel New experience level
     */
    public void setLevel(int newLevel);

    /**
     * Checks player reached required experience level
     *
     * @param requiredLevel Required experience level
     * @return {@code true} if player player did reach required level
     */
    public boolean didReachLevel(int requiredLevel);

    /**
     * Increases the player level by a specified amount.
     *
     * @apiNote
     * Never use negative amount to decrease player level, use
     * {@link #decreaseLevel(int)} instead.
     *
     * @param lvlAmount Amount of additional levels
     */
    public void increaseLevel(int lvlAmount);

    /**
     * Decreases the player level by a specified amount.
     *
     * @apiNote
     * Never use negative amount to increase player level, use
     * {@link #increaseLevel(int)} instead.
     *
     * @param lvlAmount Amount of levels to take away
     */
    public void decreaseLevel(int lvlAmount);

    /**
     * Gets player's total experience points
     *
     * @return Total experience points
     */
    public int getTotalExp();

    /**
     * Sets player's total experience points
     *
     * @param newTotalExperience New total experience
     */
    public void setTotalExp(int newTotalExperience);

    /**
     * Gets player's current level experience points
     *
     * @apiNote
     * This method returns experience on current level, to get total player
     * experience use {@link #getTotalExp()}
     *
     * @return Current level experience points
     */
    public int getExp();

    /**
     * Sets player's current level experience points
     *
     * @apiNote
     * This method changes experience on current level, to set total player
     * experience use {@link #setTotalExp(int)}
     *
     * @param newExperience New level experience points
     */
    public void setExp(int newExperience);

    /**
     * Get the total amount of experience required for the player to reach level
     *
     * @return Experience required to level up
     */
    public int getExpToLevel();

    /**
     * Gives player the amount of experience specified
     *
     * @param expAmount Exp amount to give
     */
    public void giveExp(int expAmount);

    /**
     * Takes away player the amount of experience specified
     *
     * @param expAmount Exp amount to take away
     */
    public void takeExp(int expAmount);
}
