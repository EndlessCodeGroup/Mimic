package ru.endlesscode.mimic.bukkit.system.battlelevels

import me.robin.battlelevels.api.BattleLevelsAPI
import java.util.UUID

internal class BattleLevelsApiWrapper {
    fun getNeededFor(level: Int): Double = BattleLevelsAPI.getNeededFor(level)
    fun getLevel(uuid: UUID): Int = BattleLevelsAPI.getLevel(uuid)
    fun getScore(uuid: UUID): Double = BattleLevelsAPI.getScore(uuid)
    fun getNeededForNext(uuid: UUID): Double = BattleLevelsAPI.getNeededForNext(uuid)
    fun getNeededForNextRemaining(uuid: UUID): Double = BattleLevelsAPI.getNeededForNextRemaining(uuid)
    fun addLevel(uuid: UUID, amount: Int): Unit = BattleLevelsAPI.addLevel(uuid, amount)
    fun removeLevel(uuid: UUID, amount: Int): Unit = BattleLevelsAPI.removeLevel(uuid, amount)
    fun addScore(uuid: UUID, amount: Double): Unit = BattleLevelsAPI.addScore(uuid, amount, true)
    fun removeScore(uuid: UUID, amount: Double): Unit = BattleLevelsAPI.removeScore(uuid, amount)
}
