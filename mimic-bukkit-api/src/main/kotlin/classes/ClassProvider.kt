// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.classes

import net.kyori.adventure.key.KeyPattern
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.MimicService

/**
 * Implement it to make your plugin's classes available via [Classes].
 * Register it in `onLoad` via [Mimic.registerClassProvider][ru.endlesscode.mimic.Mimic.registerClassProvider].
 * To read or change classes of players, use [Classes] instead.
 *
 * A **slot** holds at most one of the player's classes: a class group, a primary or secondary class, a race.
 * Races are classes too, held in a race slot.
 * Your plugin may have one slot or many.
 *
 * It replaces the deprecated `ClassSystem` API, which supports only a flat list of class names.
 *
 * ### What to implement
 *
 * - Required: [allSlots], [allClasses], [getClasses] and [getPrimarySlot].
 * - Optional: [setClass]. Skip it if your plugin can't change classes, it returns `false` by default.
 *
 * ### IDs
 *
 * Mimic adds your provider [id] as the namespace, so return slot and class IDs without it:
 * `warrior` becomes `myplugin:warrior`. Don't change [id] later, it breaks consumers' configs.
 * Class IDs are unique within your provider, regardless of the slot.
 *
 * If your plugin has its own namespaced IDs, convert them to paths:
 * - Drop your plugin's own namespace: `origins:human` -> `human`.
 * - Keep other namespaces as the first path part: `myaddon:elf` -> `myaddon/elf`.
 *
 * Other rules:
 * - Allowed characters: `a-z`, `0-9`, `.`, `_`, `-` and `/`. Slots and classes with other characters are ignored.
 * - To convert other IDs, lowercase them and replace other characters with `_`.
 *   Keep a map back to the original IDs.
 * - If two IDs become the same (`Warrior` and `warrior`), log a warning and keep one of them.
 *
 * ### Rules
 *
 * - Only online players are passed to the provider.
 * - Never throw. If the player's data isn't loaded yet, return `null` or empty from reads and `false` from writes.
 * - Don't change data until the player's data is loaded, even if your plugin's API allows it.
 * - Report only active classes. Unlocked but inactive classes, classes the player has left, jobs and professions
 *   aren't classes here.
 * - Report a placeholder meaning "no class yet", like a starting class, as an empty slot,
 *   so consumers can tell whether the player has picked a class.
 *   A default that is a real choice, like a starting race, is a class.
 * - Reads may be called from any thread. If your plugin can't read data there, return `null` or empty instead of throwing.
 * - Writes are expected on the main thread (on Folia - on the player's thread), but Mimic doesn't enforce it.
 *
 * Mimic does the rest: selects the provider by the ID namespace, finds subclasses using [ClassDefinition.parents],
 * and catches errors thrown by the provider.
 *
 * @since 0.10.0
 */
public interface ClassProvider : MimicService {

    /**
     * All slots this provider knows, regardless of players.
     * The list may change at runtime, so it is read on every request.
     */
    public val allSlots: Collection<Slot>

    /**
     * All classes this provider knows, regardless of players.
     * The list may change at runtime, so it is read on every request.
     */
    public val allClasses: Collection<ClassDefinition>

    /**
     * Returns classes the [player] has, where key is a slot ID. Empty slots are omitted.
     * Return the same [ClassDefinition.parents] and [ClassDefinition.levelTrack] as in [allClasses].
     */
    public fun getClasses(player: Player): Map<String, ClassDefinition>

    /** Returns the slot holding the class that best represents the [player]'s class, or `null` if there is no such slot. */
    public fun getPrimarySlot(player: Player): Slot?

    /**
     * Sets the class with [classId] in the slot with [slotId], or in the slot chosen by your plugin if [slotId] is `null`.
     * Do the same as your plugin's admin command changing the class, with its usual side effects.
     * Check that [classId] is one of your classes, and that the class can be held in the slot.
     *
     * @return `true` if the change was requested, `false` if it is unsupported for this [player], class and slot,
     *   or the player's data isn't loaded. Return `true` even if the plugin may cancel the change through its events.
     */
    public fun setClass(
        player: Player,
        @KeyPattern.Value classId: String,
        @KeyPattern.Value slotId: String?,
    ): Boolean = false

    /**
     * Slot of a [ClassProvider]. Slots are immutable, so they can be cached and reused.
     * @property id Slot path, like `class` or `race`.
     * @property name Display name of the slot defined by the plugin.
     */
    public class Slot(
        @param:KeyPattern.Value
        public val id: String,
        public val name: Component,
    )

    /**
     * Class of a [ClassProvider]. Classes are immutable, so they can be cached and reused.
     * @property id Class path, like `warrior` or `race/elf`.
     * @property name Display name of the class defined by the plugin.
     * @property parents IDs of direct parent classes of this provider. A paladin, a subclass of a warrior,
     *   lists `warrior`. Empty if the class has no parents.
     * @property levelTrack Path of this provider's level track holding the class level while the player has the class,
     *   or `null` if the class has no level. See [LevelProvider][ru.endlesscode.mimic.level.LevelProvider].
     */
    public class ClassDefinition @JvmOverloads constructor(
        @param:KeyPattern.Value
        public val id: String,
        public val name: Component,
        public val parents: Set<String> = emptySet(),
        @param:KeyPattern.Value
        public val levelTrack: String? = null,
    )
}
