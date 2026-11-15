// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.classes

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.WrappedMimicService

internal class WrappedClassProvider(
    private val delegate: ClassProvider,
    plugin: Plugin,
) : WrappedMimicService(delegate, plugin.name), ClassProvider {

    override val allSlots: Collection<ClassProvider.Slot>
        get() = delegate.allSlots

    override val allClasses: Collection<ClassProvider.ClassDefinition>
        get() = delegate.allClasses

    override fun getClasses(player: Player): Map<String, ClassProvider.ClassDefinition> = delegate.getClasses(player)
    override fun getPrimarySlot(player: Player): ClassProvider.Slot? = delegate.getPrimarySlot(player)

    override fun setClass(player: Player, classId: String, slotId: String?): Boolean =
        delegate.setClass(player, classId, slotId)
}
