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

package ru.endlesscode.mimic

/**
 * Factory to create subsystem instances.
 *
 * This interface must be implemented for each Player System (not subsystem).
 *
 * @param T           System type
 * @param constructor Function to create system
 * @param tag         The tag
 */
open class SystemFactory<T : MimicSystem>(val tag: String, private val constructor: (Any) -> T) {

    /**
     * Creates new subsystem object with player initialization.
     *
     * @param arg Argument needed for system initialization.
     * @return Player system for specified player. Can't be null
     */
    fun get(arg: Any): T {
        return constructor(arg)
    }
}
