package ru.endlesscode.mimic.impl.quantumrpg

import org.bukkit.entity.Player
import su.nightexpress.quantumrpg.QuantumRPG
import su.nightexpress.quantumrpg.api.QuantumAPI
import su.nightexpress.quantumrpg.modules.ModuleCache
import su.nightexpress.quantumrpg.modules.ModuleItem
import su.nightexpress.quantumrpg.modules.api.QModuleDrop
import su.nightexpress.quantumrpg.modules.list.classes.ClassManager
import su.nightexpress.quantumrpg.modules.list.classes.api.RPGClass
import su.nightexpress.quantumrpg.modules.list.classes.api.UserClassData

internal typealias DropModule = QModuleDrop<out ModuleItem>

internal class QuantumRpgWrapper {

    val isEnabled: Boolean get() = QuantumRPG.instance.isEnabled

    val itemsModules: Sequence<DropModule> by lazy {
        sequenceOf(
            modules.activeItemManager,
            modules.arrowManager,
            modules.conumeManager,
            modules.resolveManager,
            modules.essenceManager,
            modules.extractManager,
            modules.fortifyManager,
            modules.gemManager,
            modules.identifyManager,
            modules.tierManager,
            modules.magicDustManager,
            modules.refineManager,
            modules.repairManager,
            modules.runeManager,
        )
    }

    private val modules: ModuleCache by lazy { QuantumAPI.getModuleManager() }
    private val userManager: ClassManager get() = modules.classManager

    fun getPlayerClass(player: Player): RPGClass? = getClassData(player).playerClass

    fun getClassData(player: Player): UserClassData = checkNotNull(userManager.getUserData(player))
}
