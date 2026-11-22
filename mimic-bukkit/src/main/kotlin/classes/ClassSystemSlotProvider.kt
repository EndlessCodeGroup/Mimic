// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // ClassSystem API is deprecated but still supported

package ru.endlesscode.mimic.classes

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import ru.endlesscode.mimic.internal.NormalizedIds
import ru.endlesscode.mimic.internal.stripColor

/**
 * Exposes the given [ClassSystem][BukkitClassSystem] [provider] as a provider of the single primary
 * [slot][SingleSlotClassProvider.DEFAULT_SLOT] holding the primary class.
 *
 * The old API can't list classes, so [allClasses] is empty.
 * Class names are converted to IDs: `§6Arcane Mage` -> `arcane_mage`, the name is kept as the display name.
 * Characters other than `a-z`, `0-9`, `.`, `_`, `-` and `/` become `_`, so non-Latin names may produce equal IDs.
 * [ClassSystem] throws [IllegalStateException] if the player's data doesn't exist, reads return empty in this case.
 */
internal class ClassSystemSlotProvider(
    private val provider: BukkitClassSystem.Provider,
) : SingleSlotClassProvider() {

    override val id: String get() = provider.id
    override val isEnabled: Boolean get() = provider.isEnabled

    override val allClasses: Collection<ClassProvider.ClassDefinition> = emptyList()

    override fun getClass(player: Player): ClassProvider.ClassDefinition? {
        val primaryClass = try {
            provider.getSystem(player).primaryClass
        } catch (_: IllegalStateException) {
            null
        }
        if (primaryClass.isNullOrEmpty()) return null
        // Old class systems may return display names with legacy color codes
        val name = LegacyComponentSerializer.legacySection().deserialize(primaryClass)
        val path = NormalizedIds.normalize(primaryClass.stripColor())
        if (path.isEmpty()) return null
        return ClassProvider.ClassDefinition(path, name)
    }
}
