package ru.endlesscode.mimic.impl.quantumrpg

import org.bukkit.entity.Player
import su.nightexpress.quantumrpg.QuantumRPG
import su.nightexpress.quantumrpg.api.QuantumAPI
import su.nightexpress.quantumrpg.modules.list.classes.ClassManager
import su.nightexpress.quantumrpg.modules.list.classes.api.RPGClass
import su.nightexpress.quantumrpg.modules.list.classes.api.UserClassData

internal class QuantumRpgWrapper {

    val isEnabled: Boolean get() = QuantumRPG.instance.isEnabled

    private val userManager: ClassManager
        get() = QuantumAPI.getModuleManager().classManager

    fun getPlayerClass(player: Player): RPGClass? = getClassData(player).playerClass

    fun getClassData(player: Player): UserClassData = checkNotNull(userManager.getUserData(player))
}
