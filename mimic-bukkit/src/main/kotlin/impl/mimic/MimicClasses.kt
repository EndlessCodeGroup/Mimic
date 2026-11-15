// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mimic

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.ServiceEntry
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.call
import ru.endlesscode.mimic.classes.CharacterClass
import ru.endlesscode.mimic.classes.ClassProvider
import ru.endlesscode.mimic.classes.ClassSlot
import ru.endlesscode.mimic.classes.Classes

internal typealias ClassProviderEntry = ServiceEntry<ClassProvider>

/**
 * Classes combining slots and classes of all class providers, using provider IDs as namespaces:
 * `warrior` -> `fabled:warrior`.
 *
 * Deprecated class systems, except the built-in `permissions`, are registered here as providers
 * of the single slot `class`, see `MimicImpl.registerClassSystem`.
 * The primary class is taken from the provider with [preferredProviderId].
 * If it isn't set, the primary class is taken from the first provider in order of priority that has one for the player.
 */
internal class MimicClasses(
    classProviders: ServiceRegistry<ClassProvider>,
    preferredProviderId: () -> String,
) : Classes {

    val providers = NamespacedProviders(classProviders, "primary class", preferredProviderId)

    override fun getAllSlots(): List<ClassSlot> = providers.enabled.flatMap { it.getAllSlots() }

    override fun getAllClasses(): List<CharacterClass> = providers.enabled.flatMap { provider ->
        provider.getAllClasses().map { provider.characterClass(it) }
    }

    override fun getClasses(player: Player): Map<Key, CharacterClass> {
        if (!player.isOnline) return emptyMap()

        return buildMap {
            for (provider in providers.enabled) {
                for ((slotId, definition) in provider.validClasses(player)) {
                    put(provider.key(slotId), provider.characterClass(definition))
                }
            }
        }
    }

    override fun getPrimaryClass(player: Player): CharacterClass? {
        if (!player.isOnline) return null
        return providers.selectPrimary { it.getPrimaryClass(player) }
    }

    override fun hasClass(player: Player, classId: Key): Boolean {
        if (!player.isOnline) return false
        val provider = findProvider(classId) ?: return false

        val path = classId.value()
        val playerClasses = provider.validClasses(player).values
        if (playerClasses.any { it.id == path }) return true

        val queue = ArrayDeque(playerClasses.flatMap { it.parents }.distinct())
        if (queue.isEmpty()) return false
        if (path in queue) return true

        // Breadth-first search through ancestors of all player classes, cycles are ignored.
        val parents = provider.call(default = emptyMap()) {
            buildMap { for (definition in allClasses) putIfAbsent(definition.id, definition.parents) }
        }
        val visited = queue.toMutableSet()
        while (queue.isNotEmpty()) {
            for (parent in parents[queue.removeFirst()].orEmpty()) {
                if (parent == path) return true
                if (visited.add(parent)) queue.add(parent)
            }
        }
        return false
    }

    override fun setClass(player: Player, classId: Key): Boolean = setClass(player, classId, slotPath = null)

    override fun setClass(player: Player, classId: Key, slotId: Key): Boolean {
        return slotId.namespace() == classId.namespace() && setClass(player, classId, slotId.value())
    }

    private fun setClass(player: Player, classId: Key, slotPath: String?): Boolean {
        if (!player.isOnline) return false
        val provider = findProvider(classId) ?: return false
        return provider.call(default = false) { setClass(player, classId.value(), slotPath) }
    }

    private fun findProvider(classId: Key): ClassProviderEntry? =
        providers.find(classId, CLASS) { path -> call(default = false) { allClasses.any { it.id == path } } }

    // Values are checked inside call, so a provider returning nulls from Java is skipped
    private fun ClassProviderEntry.getAllSlots(): List<ClassSlot> =
        call(default = emptyList()) { validValues(allSlots, SLOT) { it.id }.map { classSlot(it) } }

    /** Returns all classes of this provider, skipping classes with invalid or duplicated IDs. */
    private fun ClassProviderEntry.getAllClasses(): List<ClassProvider.ClassDefinition> =
        call(default = emptyList()) { validValues(allClasses, CLASS) { it.id } }

    /** Returns classes the [player] has, skipping entries with invalid slot or class IDs. */
    private fun ClassProviderEntry.validClasses(player: Player): Map<String, ClassProvider.ClassDefinition> =
        call(default = emptyMap()) {
            getClasses(player).filter { (slotId, definition) ->
                isValidPath(slotId, SLOT) && isValidPath(definition.id, CLASS)
            }
        }

    private fun ClassProviderEntry.getPrimaryClass(player: Player): CharacterClass? {
        val slot = call(default = null) { getPrimarySlot(player) } ?: return null
        val definition = validClasses(player)[slot.id] ?: return null
        return characterClass(definition)
    }

    private fun ClassProviderEntry.classSlot(slot: ClassProvider.Slot): ClassSlot =
        MimicClassSlot(key(slot.id), slot.name)

    private fun ClassProviderEntry.characterClass(definition: ClassProvider.ClassDefinition): CharacterClass {
        val levelTrack = definition.levelTrack
            ?.takeIf { isValidPath(it, LEVEL_TRACK) }
            ?.let { key(it) }
        return MimicCharacterClass(key(definition.id), definition.name, levelTrack)
    }

    private companion object {
        const val SLOT = "slot"
        const val CLASS = "class"
        const val LEVEL_TRACK = "level track"
    }
}

private class MimicClassSlot(
    private val key: Key,
    private val name: Component,
) : ClassSlot {
    override fun key(): Key = key
    override fun name(): Component = name
    override fun equals(other: Any?): Boolean = other is ClassSlot && key == other.key()
    override fun hashCode(): Int = key.hashCode()
    override fun toString(): String = "ClassSlot(${key.asString()})"
}

private class MimicCharacterClass(
    private val key: Key,
    private val name: Component,
    private val levelTrack: Key?,
) : CharacterClass {
    override fun key(): Key = key
    override fun name(): Component = name
    override fun levelTrack(): Key? = levelTrack
    override fun equals(other: Any?): Boolean = other is CharacterClass && key == other.key()
    override fun hashCode(): Int = key.hashCode()
    override fun toString(): String = "CharacterClass(${key.asString()})"
}

/** IDs of all registered class providers, including bridged class systems, the values of `class-provider` config option. */
internal fun Mimic.getClassProviderIds(): Set<String> = (classes as MimicClasses).providers.allIds

/** ID of the provider of the primary class. */
internal fun Mimic.getPrimaryClassProviderId(): String? = (classes as MimicClasses).providers.primary?.service?.id
