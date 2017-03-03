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

import org.bukkit.entity.Player;
import ru.endlesscode.mimic.BaseSystem;

/**
 * System that provides methods to work with players level systems.
 * You can check or change values of level and experience.
 *
 * @author Osip Fatkullin
 * @since 1.0
 */
public interface LevelSystem extends BaseSystem {
    /**
     * Gets current experience level of player
     *
     * @param player The player to get level
     * @return Current experience level
     */
    public int getPlayerLevel(Player player);

    /**
     * Sets current experience level for player
     *
     * @param player Player to set level
     * @param newLevel New experience level
     */
    public void setPlayerLevel(Player player, int newLevel);

    /**
     * Checks player reached required experience level
     *
     * @param player Player to check level
     * @param requiredLevel Required experience level
     */
    public boolean didPlayerReachLevel(Player player, int requiredLevel);

    /**
     * Increases the player level by a specified amount.
     *
     * @apiNote
     * Never use negative amount to decrease player level, use
     * {@link #decreasePlayerLevel(Player, int)} instead.
     *
     * @param player Player to increase level
     * @param lvlAmount Amount of additional levels
     */
    public void increasePlayerLevel(Player player, int lvlAmount);

    /**
     * Decreases the player level by a specified amount.
     *
     * @apiNote
     * Never use negative amount to increase player level, use
     * {@link #increasePlayerLevel(Player, int)} instead.
     *
     * @param player Player to decrease level
     * @param lvlAmount Amount of levels to take away
     */
    public void decreasePlayerLevel(Player player, int lvlAmount);

    /**
     * Gets player's total experience points
     *
     * @param player Player to get total experience
     * @return Total experience points
     */
    public int getTotalPlayerExp(Player player);

    /**
     * Sets player's total experience points
     *
     * @param player Player to set experience
     * @param newTotalExperience New total experience
     */
    public void setTotalPlayerExp(Player player, int newTotalExperience);

    /**
     * Gets player's current level experience points
     *
     * @apiNote
     * This method returns experience on current level, to get total player
     * experience use {@link #getTotalPlayerExp(Player)}
     *
     * @param player Player to get experience
     * @return Current level experience points
     */
    public int getPlayerExp(Player player);

    /**
     * Sets player's current level experience points
     *
     * @apiNote
     * This method changes experience on current level, to set total player
     * experience use {@link #setPlayerExp(Player, int)}
     *
     * @param player Player to set experience
     * @param newExperience New level experience points
     */
    public void setPlayerExp(Player player, int newExperience);

    /**
     * Get the total amount of experience required for the player to reach level
     *
     * @param player The Player
     * @return Experience required to level up
     */
    public int getPlayerExpToLevel(Player player);

    /**
     * Gives player the amount of experience specified
     *
     * @param player Player to give experience
     * @param expAmount Exp amount to give
     */
    public void giveExpToPlayer(Player player, int expAmount);

    /**
     * Takes away player the amount of experience specified
     *
     * @param player Player to take away experience
     * @param expAmount Exp amount to take away
     */
    public void takeExpFromPlayer(Player player, int expAmount);
}
