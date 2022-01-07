package ru.endlesscode.mimic.impl.mmocore

import net.Indyuce.mmocore.MMOCore
import net.Indyuce.mmocore.api.player.PlayerData
import net.Indyuce.mmocore.api.player.profess.PlayerClass
import net.Indyuce.mmocore.experience.ExpCurve
import org.bukkit.OfflinePlayer

internal class MmoCoreWrapper {
    val isEnabled: Boolean get() = MMOCore.plugin.isEnabled

    fun getData(player: OfflinePlayer): PlayerData = PlayerData.get(player)
    fun getPlayerClass(player: OfflinePlayer): PlayerClass = PlayerData.get(player).profess
    fun getExpCurve(player: OfflinePlayer): ExpCurve = PlayerData.get(player).profess.expCurve
}
