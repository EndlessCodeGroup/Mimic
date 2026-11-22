// Copyright (c) 2021 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mmocore

import net.Indyuce.mmocore.MMOCore
import net.Indyuce.mmocore.api.player.PlayerData
import net.Indyuce.mmocore.api.player.profess.PlayerClass
import net.Indyuce.mmocore.experience.Profession
import net.Indyuce.mmocore.experience.curve.ExperienceCurve
import net.Indyuce.mmocore.player.Message
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.Event
import ru.endlesscode.mimic.internal.NormalizedIds

internal class MmoCoreWrapper {
    val isEnabled: Boolean get() = MMOCore.plugin.isEnabled

    fun getData(player: OfflinePlayer): PlayerData = PlayerData.get(player)
    fun getPlayerClass(player: OfflinePlayer): PlayerClass = PlayerData.get(player).profess
    fun getExpCurve(player: OfflinePlayer): ExperienceCurve = PlayerData.get(player).profess.expCurve ?: ExperienceCurve.DEFAULT

    val classes: Collection<PlayerClass> get() = MMOCore.plugin.classManager.all
    val defaultClass: PlayerClass? get() = MMOCore.plugin.classManager.defaultClass
    val professions: Collection<Profession> get() = MMOCore.plugin.professionManager.all

    /** Returns data of the [player], or `null` if it isn't loaded. Data is added to MMOCore before it is loaded. */
    fun getLoadedData(player: Player): PlayerData? =
        MMOCore.plugin.playerDataManager.getOrNull(player)?.takeIf { it.isSessionReady }

    /** Selects the class like `/mmocore admin class select` after its event: saved progress of the class is restored. */
    fun selectClass(data: PlayerData, profess: PlayerClass) {
        data.getClassInfoOrNew(profess).load(profess, data)
        Message.CLASS_SELECT.send(data, "class", profess.name)
    }

    fun callEvent(event: Event) = Bukkit.getPluginManager().callEvent(event)
}

/** Class ID converted to a Mimic ID: `ARCANE_MAGE` becomes `arcane_mage`. */
internal val PlayerClass.classId: String
    get() = NormalizedIds.normalize(id)
