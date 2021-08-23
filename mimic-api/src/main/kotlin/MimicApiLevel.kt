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

/** Utility to check Mimic API level. */
public object MimicApiLevel {

    /**
     * # Version 0.6
     * - Mimic API levels
     * - Payload in ItemsRegistry.getItem
     */
    public const val VERSION_0_6: Int = 1

    /**
     * The latest version at the moment of Mimic COMPILATION.
     *
     * Usage of this constant will be inlined by the compiler, so use it only to save the version
     * which was used at your plugin COMPILE TIME.
     * Use [checkApiLevel] if you want to check that the current RUNNING Mimic API level meets to
     * the required.
     */
    public const val CURRENT: Int = VERSION_0_6

    /**
     * Returns 'true' if the current RUNNING Mimic API level meets to  the required, otherwise `false`.
     * ```
     * // Specify here the version required for APIs you use.
     * if (!MimicApiLevel.checkApiLevel(MimicApiLevel.VERSION_0_6)) {
     *     println("At least Mimic 0.6 is required. Please download it from {link here}")
     * }
     * ```
     */
    @JvmStatic
    public fun checkApiLevel(requiredApiLevel: Int): Boolean = requiredApiLevel <= CURRENT
}
