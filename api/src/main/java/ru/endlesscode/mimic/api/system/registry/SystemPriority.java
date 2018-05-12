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

package ru.endlesscode.mimic.api.system.registry;

/**
 * System priorities.
 *
 * <p>System with higher priority will be loaded first. Use priorities to
 * resolve conflicts.</p>
 *
 * @author Osip Fatkullin
 * @since 1.0
 */
public enum SystemPriority {
    LOWEST,
    LOW,
    NORMAL,
    HIGH,
    HIGHEST;

    public static SystemPriority fromString(String string) {
        return SystemPriority.valueOf(string.toUpperCase());
    }
}
