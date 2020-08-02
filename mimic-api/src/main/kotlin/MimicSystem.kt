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
 * This interface should be implemented by any system that should work with Mimic.
 *
 * Implementation should contain something player-related object to get data from.
 * For this object recommended use [ExistingWeakReference][ru.endlesscode.mimic.ref.ExistingWeakReference]
 */
interface MimicSystem {

    /** Returns `true` if the system is found and enabled. */
    val isEnabled: Boolean

    /**
     * Returns the ID of the system.
     * Usually used name of the plugin that implements system.
     */
    val id: String
}
