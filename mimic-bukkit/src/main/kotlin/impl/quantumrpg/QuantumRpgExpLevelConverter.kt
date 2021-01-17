package ru.endlesscode.mimic.impl.quantumrpg

import org.bukkit.entity.Player
import ru.endlesscode.mimic.level.ExpLevelConverter
import ru.endlesscode.mimic.util.ExistingWeakReference
import su.nightexpress.quantumrpg.modules.list.classes.api.RPGClass

public class QuantumRpgExpLevelConverter internal constructor(
    player: Player,
    private val quantumRPG: QuantumRpgWrapper,
) : ExpLevelConverter {

    public companion object {
        @JvmStatic
        public fun get(player: Player): ExpLevelConverter = QuantumRpgExpLevelConverter(player, QuantumRpgWrapper())
    }

    private val playerRef = ExistingWeakReference(player)

    private val playerClass: RPGClass?
        get() = quantumRPG.getPlayerClass(playerRef.get())

    override fun getExpToReachLevel(level: Int): Double {
        if (level <= 1) return 0.0
        val playerClass = playerClass ?: return -1.0
        return playerClass.getNeedExpForLevel(level).toDouble()
    }
}
