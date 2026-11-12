// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.classes

/**
 * System that provides methods to work with player's class.
 *
 * Before implementing run an eye over all default method implementations
 * and override all methods that work not properly for your case.
 */
public interface ClassSystem {

    /**
     * Primary class of the player.
     *
     * It is called "primary" because some class systems can support many classes
     * for one player. Primary class can't be null if player has at least one class.
     *
     * @return Primary class name or `null` if player has no any classes.
     */
    public val primaryClass: String?
        get() = classes.firstOrNull()

    /**
     * List of player classes.
     *
     * It is actual for systems which support many classes for one player.
     * If system doesn't support - it just returns [List] with one element.
     *
     * Never returns `null`, but can return empty [List].
     *
     * @return List of player's classes
     * @throws IllegalStateException If player-related object not exists.
     */
    public val classes: List<String>

    /** Returns `true` if player has any class. */
    public fun hasAnyClass(): Boolean = classes.isNotEmpty()

    /**
     * Returns `true` if player has any of required classes.
     * @since 0.6.2
     */
    public fun hasAnyOfClasses(firstClass: String, vararg restClasses: String): Boolean {
        return hasAnyOfClasses(listOf(firstClass, *restClasses))
    }

    /** Returns `true` if player has any of [requiredClasses]. */
    public fun hasAnyOfClasses(requiredClasses: List<String>): Boolean {
        return requiredClasses.any { hasClass(it) }
    }

    /**
     * Returns `true` if player has all required classes.
     * @since 0.6.2
     */
    public fun hasAllClasses(firstClass: String, vararg restClasses: String): Boolean {
        return hasAllClasses(listOf(firstClass, *restClasses))
    }

    /** Returns `true` if player has all [requiredClasses]. */
    public fun hasAllClasses(requiredClasses: List<String>): Boolean {
        return requiredClasses.all { hasClass(it) }
    }

    /** Returns `true` if player has [requiredClass]. */
    public fun hasClass(requiredClass: String): Boolean {
        return classes.any {
            it.equals(requiredClass, ignoreCase = true)
        }
    }
}
