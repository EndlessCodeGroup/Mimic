package ru.endlesscode.mimic.impl.quantumrpg

import org.bukkit.entity.Player
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.level.ExpLevelConverter
import su.nightexpress.quantumrpg.modules.list.classes.api.UserClassData
import kotlin.math.roundToInt

public class QuantumRpgLevelSystem private constructor(
    player: Player,
    private val quantumRpg: QuantumRpgWrapper,
) : BukkitLevelSystem(player) {

    public companion object {
        public const val ID: String = "quantumrpg"
    }

    override val converter: ExpLevelConverter = QuantumRpgExpLevelConverter(player, quantumRpg)

    private val classData: UserClassData
        get() = quantumRpg.getClassData(player)

    override var level: Int
        get() = classData.level
        set(value) {
            classData.level = value
        }

    override var exp: Double
        get() = classData.exp.toDouble()
        set(value) {
            classData.exp = value.coerceIn(0.0, totalExpToNextLevel).roundToInt()
        }

    override val expToNextLevel: Double
        get() = classData.getExpToUp(false).toDouble()

    override val totalExpToNextLevel: Double
        get() = classData.getExpToUp(true).toDouble()

    override fun takeExp(expAmount: Double) {
        giveExp(-expAmount.coerceAtMost(totalExp))
    }

    override fun giveExp(expAmount: Double) {
        classData.addExp(expAmount.roundToInt())
    }

    internal class Provider : BukkitLevelSystem.Provider(ID) {
        override val isEnabled: Boolean get() = quantumRpg.isEnabled
        private val quantumRpg = QuantumRpgWrapper()

        override fun getSystem(player: Player): BukkitLevelSystem = QuantumRpgLevelSystem(player, quantumRpg)
    }
}
