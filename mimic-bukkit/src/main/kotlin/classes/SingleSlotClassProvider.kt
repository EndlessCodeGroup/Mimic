// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.classes

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/**
 * Provider of the single primary slot, for plugins with one class per player.
 * Implement the methods without slot ID, requests for other slots return empty or `false`.
 */
internal abstract class SingleSlotClassProvider(
    slotId: String = DEFAULT_SLOT,
    name: Component = Component.text("Class"),
) : ClassProvider {

    private val slot = ClassProvider.Slot(slotId, name)

    final override val allSlots: Collection<ClassProvider.Slot> = listOf(slot)

    final override fun getPrimarySlot(player: Player): ClassProvider.Slot = slot

    final override fun getClasses(player: Player): Map<String, ClassProvider.ClassDefinition> =
        getClass(player)?.let { mapOf(slot.id to it) }.orEmpty()

    final override fun setClass(player: Player, classId: String, slotId: String?): Boolean =
        (slotId == null || slotId == slot.id) && setClass(player, classId)

    abstract fun getClass(player: Player): ClassProvider.ClassDefinition?
    open fun setClass(player: Player, classId: String): Boolean = false

    companion object {
        const val DEFAULT_SLOT = "class"
    }
}
