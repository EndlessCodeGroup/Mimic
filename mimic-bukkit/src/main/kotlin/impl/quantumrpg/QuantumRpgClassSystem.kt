package ru.endlesscode.mimic.impl.quantumrpg

import org.bukkit.entity.Player
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.internal.stripColor
import su.nightexpress.quantumrpg.modules.list.classes.api.RPGClass

public class QuantumRpgClassSystem private constructor(
    player: Player,
    private val quantumRpg: QuantumRpgWrapper,
) : BukkitClassSystem(player) {

    override val primaryClass: String?
        get() = quantumRpg.getPlayerClass(player)?.colorlessName

    override val classes: List<String>
        get() {
            val playerClass = quantumRpg.getPlayerClass(player) ?: return emptyList()
            return (sequenceOf(playerClass) + playerClass.parents.asSequence())
                .map { it.colorlessName }
                .toList()
        }

    private val RPGClass.colorlessName: String get() = name.stripColor()

    internal class Provider : BukkitClassSystem.Provider {
        private val quantumRpg = QuantumRpgWrapper()

        override val isEnabled: Boolean get() = quantumRpg.isEnabled
        override val id: String = ID

        override fun getSystem(player: Player): BukkitClassSystem {
            return QuantumRpgClassSystem(player, quantumRpg)
        }
    }

    public companion object {
        public const val ID: String = "quantumrpg"
    }
}
