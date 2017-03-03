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

package ru.endlesscode.mimic;

/**
 * This interface should be implemented by any system that should work with Mimic.
 *
 * @author  Osip Fatkullin
 * @since   1.0
 */
public interface BaseSystem {
    /**
     * Returns the name of system.
     *
     * @implNote
     * Usually used name of the plugin that implements system.
     *
     * @return name of system
     */
    public String getName();

    /**
     * Checks if this system is found and enabled
     *
     * @return {@code true} if works, otherwise {@code false}
     */
    public boolean isEnabled();
}
