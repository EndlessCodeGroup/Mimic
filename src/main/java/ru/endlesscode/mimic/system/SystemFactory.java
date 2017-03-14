/*
 * This file is part of MimicAPI.
 * Copyright (C) 2017 Osip Fatkullin
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

package ru.endlesscode.mimic.system;

import org.bukkit.entity.Player;

/**
 * Abstract factory for getting player systems
 *
 * <p>Implementation should be annotated by {@link ru.endlesscode.mimic.system.Metadata}
 * </p>
 *
 * @author Osip Fatkullin
 * @since 1.0
 */
public interface SystemFactory {
    /**
     * Gets player level system
     *
     * @implSpec
     * If system not implemented, just
     *
     * @param player    Player to get level system
     * @return Level system for specified player
     */
    public LevelSystem getLevelSystem(Player player);

    /**
     * Gets player class system
     *
     * @param player    Player to get class system
     * @return Class system for specified player
     */
    public ClassSystem getClassSystem(Player player);

    /**
     * Returns the name of system.
     *
     * @implNote
     * Usually used name of the plugin that implements system.
     *
     * @return name of system
     */
    public String getName();
}
