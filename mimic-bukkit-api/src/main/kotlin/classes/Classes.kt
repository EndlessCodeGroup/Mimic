// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.classes

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.LookupResult
import ru.endlesscode.mimic.LookupStatus

/**
 * Classes of players from all RPG plugins supported by Mimic. Get it via [Mimic.classes][ru.endlesscode.mimic.Mimic.classes].
 * To make your own plugin's classes available here, implement [ClassProvider] instead.
 *
 * A **slot** holds at most one of the player's classes: a class group, a primary or secondary class, a race.
 * Races are classes too, held in a race slot.
 * A player may have classes from several plugins at once, for example a class from one plugin and a race from another.
 *
 * It replaces the deprecated `ClassSystem` API, which supports only a flat list of class names.
 *
 * ### Quick start
 *
 * ```java
 * // Check the class, a paladin is a warrior too if it is a subclass of a warrior
 * if (classes.hasClass(player, Key.key("fabled:warrior"))) { ... }
 *
 * // Check the level of the player's class, server owners choose its plugin in Mimic config
 * CharacterClass playerClass = classes.getPrimaryClass(player);
 * if (playerClass == null) return; // The player hasn't picked a class yet
 * Key levelTrack = playerClass.levelTrack();
 * if (levelTrack != null && levels.hasLevel(player, levelTrack, 20)) { ... }
 * ```
 *
 * ### IDs
 *
 * Slots and classes are identified by [Key], which consists of a namespace and a path:
 * - The namespace is the ID of the plugin providing the slot or class: `fabled`, `heroes`.
 * - The path is the slot or class within that plugin: `race`, `warrior`.
 *
 * For example, a class plugin may provide slots `fabled:class` and `fabled:race`,
 * and classes `fabled:warrior` and `fabled:elf`.
 *
 * The namespace is required, since the same path in different plugins means a different class.
 * Note that `Key.key("warrior")` turns into `minecraft:warrior`,
 * so read class IDs from configs with [lookupClass].
 * Classes of plugins using the deprecated `ClassSystem` API are missing from [getAllClasses].
 *
 * ### Rules
 *
 * - Works only with online players.
 * - `null`, `false` or empty means "not possible right now": the class doesn't exist, the plugin doesn't support
 *   this action, or the player's data isn't loaded yet.
 * - A slot holds at most one class. An empty slot is normal: the player hasn't picked a class there yet.
 * - Don't cache results. Read them again each time you need them.
 * - Read from any thread. Change classes only on the main thread (on Folia - on the player's thread).
 *
 * @since 0.10.0
 */
public interface Classes {

    /**
     * Returns all slots of all providers, regardless of players.
     * Slots are sorted by provider priority, then in the order the provider returns them.
     */
    public fun getAllSlots(): List<ClassSlot>

    /**
     * Looks up the slot ID by a [value] from a config. The result has only IDs of existing slots.
     * Use it instead of `Key.key(String)`, which turns `race` into `minecraft:race`.
     *
     * The lookup is lenient:
     * - Letter case doesn't matter, and characters not allowed in IDs are replaced with `_`: `Fabled:Race` is `fabled:race`.
     * - The namespace may be omitted if only one plugin has a slot with this name: `secondary` is `heroes:secondary`.
     *   If several plugins have it, the plugin set in the `class-provider` option of Mimic config is used.
     *   If the option isn't set, or that plugin doesn't have the slot, the result is [LookupStatus.AMBIGUOUS].
     *
     * The built-in `permissions` class system has no slots, so `permissions:class` is never found.
     *
     * May be called from any thread.
     *
     * @since 0.12.0
     */
    public fun lookupSlot(value: String): LookupResult

    /**
     * Returns all classes of all providers, regardless of players. Use it for discovery and config validation.
     * Classes are sorted by provider priority, then in the order the provider returns them.
     */
    public fun getAllClasses(): List<CharacterClass>

    /**
     * Looks up the class ID by a [value] from a config. The result has only IDs of existing classes.
     * Use it instead of `Key.key(String)`, which turns `warrior` into `minecraft:warrior`.
     *
     * The lookup is lenient:
     * - Letter case doesn't matter, and characters not allowed in IDs are replaced with `_`:
     *   `Fabled:Honor Guard` is `fabled:honor_guard`.
     * - The namespace may be omitted if only one plugin has a class with this name: `honor guard` is `fabled:honor_guard`.
     *   If several plugins have it, the plugin set in the `class-provider` option of Mimic config is used.
     *   If the option isn't set, or that plugin doesn't have the class, the result is [LookupStatus.AMBIGUOUS].
     *
     * Classes of plugins using the deprecated `ClassSystem` API can't be listed, so they are never found.
     *
     * May be called from any thread.
     *
     * @since 0.12.0
     */
    public fun lookupClass(value: String): LookupResult

    /**
     * Returns classes the [player] has, where key is a slot ID. Empty slots are omitted.
     * Entries are sorted by provider priority, then in the order the provider returns them.
     */
    public fun getClasses(player: Player): Map<Key, CharacterClass>

    /**
     * Returns the class that best represents the [player]'s class, or `null` if there is no such class.
     *
     * Server owners choose the plugin for it with the `class-provider` option in Mimic config.
     * If the option isn't set, or the plugin isn't installed, Mimic asks plugins one by one,
     * from the highest priority, and returns the first primary class it gets.
     */
    public fun getPrimaryClass(player: Player): CharacterClass?

    /** Returns `true` if the [player] has the class with [classId] or any of its subclasses in any slot. */
    public fun hasClass(player: Player, classId: Key): Boolean

    /**
     * Sets the class with [classId] in the slot chosen by the plugin.
     * It works like the plugin's admin command changing the class, with its usual side effects:
     * the plugin may keep or reset the class progress, give items, or run commands.
     *
     * @return `true` if the change was requested, `false` if it is unsupported or the player's data isn't loaded.
     *   `true` doesn't guarantee the change: another plugin may cancel it, and Mimic can't detect that.
     */
    public fun setClass(player: Player, classId: Key): Boolean

    /**
     * Sets the class with [classId] in the slot with [slotId]. Works like [setClass] without a slot.
     *
     * @return `true` if the change was requested, `false` if it is unsupported, the player's data isn't loaded,
     *   or the slot and the class are from different plugins.
     *   `true` doesn't guarantee the change: another plugin may cancel it, and Mimic can't detect that.
     */
    public fun setClass(player: Player, classId: Key, slotId: Key): Boolean
}

/**
 * Class slot available via [Classes]. Slots are equal if their keys are equal.
 * @since 0.10.0
 */
public interface ClassSlot : Keyed {

    /** Full slot ID, where namespace is the provider ID: `fabled:race`. */
    override fun key(): Key

    /** Display name of the slot defined by the plugin. */
    public fun name(): Component
}

/**
 * Class available via [Classes]. Classes are equal if their keys are equal.
 * @since 0.10.0
 */
public interface CharacterClass : Keyed {

    /** Full class ID, where namespace is the provider ID: `fabled:warrior`. */
    override fun key(): Key

    /** Display name of the class defined by the plugin. */
    public fun name(): Component

    /**
     * ID of the level track holding the class level while the player has the class, or `null` if the class has no level.
     * Use it with [Levels][ru.endlesscode.mimic.level.Levels].
     */
    public fun levelTrack(): Key?
}
