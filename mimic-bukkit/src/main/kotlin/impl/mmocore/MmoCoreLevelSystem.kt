package ru.endlesscode.mimic.impl.mmocore

import net.Indyuce.mmocore.api.player.PlayerData
import net.Indyuce.mmocore.experience.EXPSource
import org.bukkit.entity.Player
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.level.ExpLevelConverter
import kotlin.math.roundToInt

/** Implementation of LevelSystem using MMOCore. */
public class MmoCoreLevelSystem private constructor(
    player: Player,
    private val mmoCore: MmoCoreWrapper,
) : BukkitLevelSystem(player) {

    override val converter: ExpLevelConverter = MmoCoreExpLevelConverter(player, mmoCore)

    override var level: Int
        get() = playerData.level
        set(value) {
            playerData.level = value
        }

    override var exp: Double
        get() = playerData.experience.toDouble()
        set(value) {
            playerData.experience = value.roundToInt()
        }

    override val totalExpToNextLevel: Double
        get() = playerData.levelUpExperience.toDouble()

    override fun takeLevels(lvlAmount: Int) {
        playerData.takeLevels(lvlAmount)
    }

    override fun giveLevels(lvlAmount: Int) {
        playerData.giveLevels(lvlAmount, EXPSource.OTHER)
    }

    override fun giveExp(expAmount: Double) {
        playerData.giveExperience(expAmount.roundToInt(), EXPSource.OTHER)
    }

    private val playerData: PlayerData
        get() = mmoCore.getData(player)

    internal class Provider : BukkitLevelSystem.Provider {
        private val mmoCore = MmoCoreWrapper()

        override val isEnabled: Boolean get() = mmoCore.isEnabled
        override val id: String = ID

        override fun getSystem(player: Player): BukkitLevelSystem = MmoCoreLevelSystem(player, mmoCore)
    }

    public companion object {
        public const val ID: String = "mmocore"
    }
}
