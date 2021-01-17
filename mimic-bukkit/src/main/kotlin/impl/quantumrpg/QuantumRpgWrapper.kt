package ru.endlesscode.mimic.impl.quantumrpg

import org.bukkit.entity.Player
import su.nexmedia.engine.data.users.IUserManager
import su.nightexpress.quantumrpg.QuantumRPG
import su.nightexpress.quantumrpg.data.api.RPGUser
import su.nightexpress.quantumrpg.modules.list.classes.api.RPGClass
import su.nightexpress.quantumrpg.modules.list.classes.api.UserClassData

internal class QuantumRpgWrapper {

    val isEnabled: Boolean get() = QuantumRPG.instance.isEnabled

    private val userManager: IUserManager<QuantumRPG, RPGUser>
        get() = QuantumRPG.instance.userManager

    fun getPlayerClass(player: Player): RPGClass? {
        return getClassData(player)?.playerClass
    }

    private fun getClassData(player: Player): UserClassData? {
        return userManager.getOrLoadUser(player)?.activeProfile?.classData
    }
}
