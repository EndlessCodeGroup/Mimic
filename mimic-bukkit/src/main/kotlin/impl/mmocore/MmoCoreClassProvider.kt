// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mmocore

import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent
import net.Indyuce.mmocore.api.player.PlayerData
import net.Indyuce.mmocore.api.player.profess.PlayerClass
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import ru.endlesscode.mimic.classes.ClassProvider
import ru.endlesscode.mimic.classes.SingleSlotClassProvider

/**
 * MMOCore class in the single slot `mmocore:class`: `ARCANE_MAGE` becomes `mmocore:arcane_mage`.
 * The default class is an empty slot. Parents are taken from subclasses of other classes.
 * The class level is the track `mmocore:level`.
 */
internal class MmoCoreClassProvider(
    private val mmoCore: MmoCoreWrapper = MmoCoreWrapper(),
) : SingleSlotClassProvider() {

    override val id: String = ID
    override val isEnabled: Boolean get() = mmoCore.isEnabled

    override val allClasses: Collection<ClassProvider.ClassDefinition>
        get() {
            val classes = selectableClasses()
            return classes.map { it.definition(classes) }
        }

    override fun getClass(player: Player): ClassProvider.ClassDefinition? {
        val profess = player.loadedData()?.profess ?: return null
        val classes = selectableClasses()
        return if (profess in classes) profess.definition(classes) else null
    }

    override fun setClass(player: Player, classId: String): Boolean {
        val data = player.loadedData() ?: return false
        val profess = selectableClasses().find { it.classId == classId } ?: return false
        if (data.profess == profess) return true

        // The same as MMOCore's `/mmocore admin class select`: progress of the class is restored if it was saved
        val event = PlayerChangeClassEvent(data, profess, PlayerChangeClassEvent.Reason.COMMAND_SELECT)
        mmoCore.callEvent(event)
        if (event.isCancelled) return true
        mmoCore.selectClass(data, profess)
        return true
    }

    private fun Player.loadedData(): PlayerData? = mmoCore.getLoadedData(this)

    /** Classes except the default one, which means "no class". */
    private fun selectableClasses(): List<PlayerClass> {
        val defaultClass = mmoCore.defaultClass
        return mmoCore.classes.filter { it != defaultClass }
    }

    companion object {
        const val ID = "mmocore"
    }
}

// MMOCore converts class names to legacy colors on load
private val LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection()

/** Definition of this class, parents are the [classes] listing it as a subclass. */
private fun PlayerClass.definition(classes: List<PlayerClass>): ClassProvider.ClassDefinition =
    ClassProvider.ClassDefinition(
        classId,
        LEGACY_SERIALIZER.deserialize(name),
        parents = classes
            .filter { parent -> parent.subclasses.any { it.profess == this } }
            .mapTo(mutableSetOf()) { it.classId },
        levelTrack = MmoCoreLevelProvider.LEVEL_TRACK_ID,
    )
