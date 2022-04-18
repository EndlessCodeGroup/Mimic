/*
 * This file is part of Mimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
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

package ru.endlesscode.mimic

/** This interface should be implemented by any service related to Mimic. */
public interface MimicService {

    /**
     * Returns `true` if the service is initialized may be used.
     * Default implementation always returns `true`.
     */
    public val isEnabled: Boolean
        get() = true

    /**
     * Returns the ID of the service. It should match regex `[a-z0-9._-]`.
     *
     * ID should be unique, so it is recommended to use the plugin name as ID.
     * Default implementation uses plugin name as an ID.
     */
    public val id: String
        get() = USE_PLUGIN_NAME_AS_ID

    public companion object {
        /**
         * Empty string means that plugin name will be used as a service ID.
         * It should be mapped on implementation wrapper layer.
         */
        public const val USE_PLUGIN_NAME_AS_ID: String = ""
    }
}
