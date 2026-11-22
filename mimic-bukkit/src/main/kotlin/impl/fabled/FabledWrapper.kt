// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.fabled

import org.bukkit.entity.Player
import ru.endlesscode.mimic.internal.NormalizedIds
import studio.magemonkey.fabled.Fabled
import studio.magemonkey.fabled.api.classes.FabledClass
import studio.magemonkey.fabled.api.player.PlayerClass
import studio.magemonkey.fabled.api.player.PlayerData
import studio.magemonkey.fabled.cmd.CmdChangeClass
import studio.magemonkey.fabled.listener.MainListener

internal class FabledWrapper {
    val isEnabled: Boolean get() = Fabled.isLoaded()
    val isManaEnabled: Boolean get() = Fabled.getSettings().isManaEnabled

    /** Class groups having at least one class. */
    val groups: List<String> get() = Fabled.getGroups()
    val classes: Collection<FabledClass> get() = Fabled.getClasses().values

    /**
     * Returns data of the active account of the [player], or `null` if it isn't loaded.
     * `Fabled.getData` loads the data from the storage, and with the SQL delay the data is replaced after the delay.
     */
    fun getLoadedData(player: Player): PlayerData? {
        if (!Fabled.hasPlayerData(player) || player.uniqueId in MainListener.loadingPlayers) return null
        return Fabled.getPlayerAccounts(player)?.activeData
    }

    /** Changes the class like `/class changeclass`: the level is kept, skills and effects of the player are restarted. */
    fun changeClass(player: Player, playerClass: PlayerClass, target: FabledClass) {
        CmdChangeClass.unload(player)
        playerClass.setClassData(target)
        playerClass.playerData.init(player)
    }
}

/** Class name converted to an ID: `Honor Guard` becomes `honor_guard`. */
internal val FabledClass.classId: String
    get() = NormalizedIds.normalize(name)

/** Group name converted to an ID of the slot and the level track. Fabled doesn't lowercase groups from configs. */
internal fun groupId(group: String): String = NormalizedIds.normalize(group)
