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

import org.jetbrains.annotations.*;
import ru.endlesscode.mimic.api.ref.*;

/**
 * This interface should be implemented by any system that should work
 * with Mimic.
 *
 * <p>Implementation should contain something player-related object to
 * get data from. For this object recommended use
 * {@link ExistingWeakReference}
 * </p>
 *
 * @author Osip Fatkullin
 * @since 1.0
 */
public interface PlayerSystem {
    /**
     * Returns player-related object.
     *
     * @return Player-related object
     */
    public @NotNull Object getHandler();

    /**
     * Checks if this system is found and enabled.
     *
     * @return {@code true} if works, otherwise {@code false}
     */
    public boolean isEnabled();

    /**
     * Returns the name of system.
     *
     * @implNote
     * Usually used name of the plugin that implements system.
     *
     * @return name of system
     */
    public @NotNull String getName();
}
