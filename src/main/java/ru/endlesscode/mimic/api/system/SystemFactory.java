/*
 * This file is part of MimicAPI.
 * Copyright (C) 2017 Osip Fatkullin
 * Copyright (C) 2017 EndlessCode Group and Contributors
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

import java.util.function.Function;

/**
 * Factory to create subsystem instances.
 *
 * @implSpec
 * This interface must be implemented for each Player System
 * (not subsystem).
 *
 * @param <T> SystemType
 * @author Osip Fatkullin
 * @since 1.1
 */
public class SystemFactory<T extends PlayerSystem> {
    private final Function<Object, ? extends T> constructor;

    /**
     * Creates factory for constructing system.
     *
     * @param constructor Function to create system
     */
    public SystemFactory(Function<Object, ? extends T> constructor) {
        this.constructor = constructor;
    }

    /**
     * Creates new subsystem object with player initialization.
     *
     * @param playerArg Player object
     * @return Player system for specified player
     */
    public T get(Object playerArg) {
        return constructor.apply(playerArg);
    }
}
