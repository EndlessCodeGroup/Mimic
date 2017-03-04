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

package ru.endlesscode.mimic.classes;

import org.bukkit.entity.Player;
import ru.endlesscode.mimic.BaseSystem;

import java.util.List;

/**
 * System that provides methods to work with players class systems.
 *
 * @author Osip Fatkullin
 * @since 1.0
 */
public interface ClassSystem extends BaseSystem {
    /**
     * Checks player has any class
     *
     * @return {@code true} if player has any class
     */
    public boolean playerHasClass();

    /**
     * Check player has required class
     *
     * @param player        Player to check class
     * @param requiredClass Required class name
     * @return {@code true} if player has required class
     */
    public boolean playerHasRequiredClass(Player player, String requiredClass);

    /**
     * Gets primary class for player.
     *
     * @apiNote
     * Class is called "primary" because some systems can support many classes
     * for one player.
     *
     * @param player Player to get class
     * @return Primary class name
     */
    public String getPlayerPrimaryClass(Player player);

    /**
     * Gets {@code Lost} of player classes
     *
     * @apiNote
     * This method actual for systems which support many classes for one player.
     * If system not support - it just return {@code List} with one element.
     *
     * @implSpec
     * Method shouldn't return {@code null}, but can return empty {@code List}
     *
     * @param player Player to get classes
     * @return {@code List} of player classes names
     */
    public List<String> getPlayerClasses(Player player);
}
