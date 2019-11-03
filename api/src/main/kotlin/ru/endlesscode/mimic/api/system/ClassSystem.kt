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

package ru.endlesscode.mimic.api.system

/**
 * System that provides methods to work with players class systems.
 *
 * Before implementing run an eye over all default method implementations
 * and override all methods that works not properly for your case.
 *
 * @see [PlayerSystem] To read more about implementation.
 * @author Osip Fatkullin
 * @since 0.1
 */
interface ClassSystem : PlayerSystem {

    /**
     * Primary class of the player.
     *
     * It is called "primary" because some class systems can support many classes
     * for one player. Primary class can't be null if player has at least one class.
     *
     * @return Primary class name or `null` if player has not any classes.
     */
    @JvmDefault
    val primaryClass: String?
        get() {
            val classes = this.classes
            return if (classes.isEmpty()) null else classes[0]
        }

    /**
     * List of player classes.
     *
     * It is actual for systems which support many classes for one player.
     * If system doesn't support - it just returns [List] with one element.
     *
     * Never returns `null`, but can return empty [List].
     *
     * @return List of player system names
     * @throws IllegalStateException If player-related object not exists.
     */
    val classes: List<String>

    /** Returns `true` if player has any class. */
    @JvmDefault
    fun hasClass(): Boolean = primaryClass != null

    /** Returns `true` if player has any of [requiredClasses]. */
    @JvmDefault
    fun hasOneOfRequiredClasses(requiredClasses: List<String>): Boolean {
        return requiredClasses.any { this.hasRequiredClass(it) }
    }

    /** Returns `true` if player has all [requiredClasses]. */
    @JvmDefault
    fun hasAllRequiredClasses(requiredClasses: List<String>): Boolean {
        return requiredClasses.all { this.hasRequiredClass(it) }
    }

    /** Returns `true` if player has [requiredClass]. */
    @JvmDefault
    fun hasRequiredClass(requiredClass: String): Boolean = requiredClass in this.classes

    /** Factory of class systems. */
    class Factory(tag: String, constructor: (Any) -> ClassSystem)
        : SystemFactory<ClassSystem>(tag, constructor)
}
