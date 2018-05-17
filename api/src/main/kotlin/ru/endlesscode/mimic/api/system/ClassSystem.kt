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

import java.util.function.Function

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
     * Gets primary class for player.
     *
     * @apiNote
     * Class is called "primary" because some systems can support many classes
     * for one player.
     *
     * @implSpec
     * Method shouldn't return null, but can return empty string
     *
     * @return Primary class name
     */
    @JvmDefault
    val primaryClass: String
        get() {
            val classes = this.classes
            return if (classes.isEmpty()) "" else classes[0]
        }

    /**
     * Gets [List] of player system.
     *
     * @apiNote
     * This method actual for systems which support many classes for one player.
     * If system not support - it just return [List] with one element.
     *
     * @implSpec
     * Method shouldn't return null, but can return empty [List].
     * Also must not contain null objects.
     *
     * @return List of player system names
     * @throws IllegalStateException If player-related object not exists.
     */
    val classes: List<String>

    /**
     * Checks player has any class.
     *
     * @return true if player has any class
     */
    @JvmDefault
    fun hasClass(): Boolean {
        return !this.primaryClass.isEmpty()
    }

    /**
     * Checks player has one of required classes.
     *
     * @param requiredClasses List of required classes
     * @return true if player has one of required class
     */
    @JvmDefault
    fun hasOneOfRequiredClasses(requiredClasses: List<String>): Boolean {
        return requiredClasses.any { this.hasRequiredClass(it) }
    }

    /**
     * Checks player has all required classes.
     *
     * @param requiredClasses List of required classes
     * @return true if player has all required class
     */
    @JvmDefault
    fun hasAllRequiredClasses(requiredClasses: List<String>): Boolean {
        return requiredClasses.all { this.hasRequiredClass(it) }
    }

    /**
     * Checks player has required class.
     *
     * @param requiredClass Required class name
     * @return true if player has required class
     */
    @JvmDefault
    fun hasRequiredClass(requiredClass: String): Boolean {
        return requiredClass in this.classes
    }

    /**
     * Factory of class systems.
     */
    class Factory(constructor: Function<Any, out ClassSystem>, tag: String) : SystemFactory<ClassSystem>(constructor, tag)
}
