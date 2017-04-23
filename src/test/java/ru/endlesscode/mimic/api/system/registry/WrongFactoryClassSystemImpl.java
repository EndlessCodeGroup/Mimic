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

package ru.endlesscode.mimic.api.system.registry;

import org.jetbrains.annotations.NotNull;
import ru.endlesscode.mimic.api.system.ClassSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Osip Fatkullin
 * @since 1.0
 */
@Metadata
public class WrongFactoryClassSystemImpl extends ClassSystem {
    private WrongFactoryClassSystemImpl() {
        // Wrong constructor
    }

    /**
     * Checks if this system is found and enabled
     *
     * @return {@code true} if works, otherwise {@code false}
     */
    @Override
    public boolean isEnabled() {
        return false;
    }

    /**
     * Returns player-related object
     *
     * @return Player-related object
     */
    @Override
    public @NotNull Object getHandler() {
        return this;
    }

    /**
     * Returns the name of system.
     *
     * @return name of system
     * @implNote Usually used name of the plugin that implements system.
     */
    @Override
    public String getName() {
        return "System Without Constructor";
    }

    /**
     * Gets {@code Lost} of player system
     *
     * @return {@code List} of player system names
     * @throws IllegalStateException If player-related object not exists.
     * @apiNote This method actual for systems which support many system for one player.
     * If system not support - it just return {@code List} with one element.
     * @implSpec Method shouldn't return {@code null}, but can return empty {@code List}.
     * Also must not contain null objects.
     */
    @Override
    public @NotNull List<String> getClasses() {
        return new ArrayList<>();
    }

    public static abstract class WrongInnerClass {}
}
