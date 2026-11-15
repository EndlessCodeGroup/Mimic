// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.fabled

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import ru.endlesscode.mimic.classes.ClassProvider
import studio.magemonkey.fabled.api.classes.FabledClass
import studio.magemonkey.fabled.api.player.PlayerData

/**
 * Fabled classes in slots of class groups: `fabled:class`, `fabled:race`. `Honor Guard` becomes `fabled:honor_guard`.
 * The primary slot is the group of the main class.
 * The class level is the track of the group: it is kept when the player professes into a subclass.
 */
internal class FabledClassProvider(
    private val fabled: FabledWrapper = FabledWrapper(),
) : ClassProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = fabled.isEnabled

    override val allSlots: Collection<ClassProvider.Slot>
        get() = fabled.groups.map(::slot)

    override val allClasses: Collection<ClassProvider.ClassDefinition>
        get() = fabled.classes.map { it.definition }

    override fun getClasses(player: Player): Map<String, ClassProvider.ClassDefinition> {
        val data = player.loadedData() ?: return emptyMap()
        return fabled.groups
            .mapNotNull { group -> data.getClass(group)?.let { groupId(group) to it.data.definition } }
            .toMap()
    }

    override fun getPrimarySlot(player: Player): ClassProvider.Slot? =
        player.loadedData()?.mainClass?.let { slot(it.data.group) }

    override fun setClass(player: Player, classId: String, slotId: String?): Boolean {
        val data = player.loadedData() ?: return false
        val target = fabled.classes.find { it.classId == classId } ?: return false
        if (slotId != null && slotId != groupId(target.group)) return false

        val current = data.getClass(target.group)
        if (current?.data == target) return true

        when {
            // The same as Fabled's `/class forceprofess`: a base class into an empty group,
            // or a subclass of the current class on its max level
            data.canProfess(target) -> data.profess(target)
            // Fabled's admin commands can't put a subclass into an empty group
            current == null -> return false
            else -> fabled.changeClass(player, current, target)
        }
        return true
    }

    private fun Player.loadedData(): PlayerData? = fabled.getLoadedData(this)

    companion object {
        const val ID = "fabled"
    }
}

// Fabled converts class prefixes to legacy colors on load
private val LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection()

// Groups don't have display names
private fun slot(group: String): ClassProvider.Slot = ClassProvider.Slot(groupId(group), Component.text(group))

private val FabledClass.definition: ClassProvider.ClassDefinition
    get() = ClassProvider.ClassDefinition(
        classId,
        LEGACY_SERIALIZER.deserialize(prefix),
        parents = setOfNotNull(parent?.classId),
        levelTrack = groupId(group),
    )
