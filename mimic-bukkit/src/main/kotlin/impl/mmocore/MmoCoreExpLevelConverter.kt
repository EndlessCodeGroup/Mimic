package ru.endlesscode.mimic.impl.mmocore

import net.Indyuce.mmocore.experience.ExpCurve
import org.bukkit.OfflinePlayer
import ru.endlesscode.mimic.level.ExpLevelConverter
import ru.endlesscode.mimic.util.ExistingWeakReference

public class MmoCoreExpLevelConverter internal constructor(
    player: OfflinePlayer,
    private val mmoCore: MmoCoreWrapper,
) : ExpLevelConverter {

    public companion object {
        @JvmStatic
        public fun get(player: OfflinePlayer): ExpLevelConverter = MmoCoreExpLevelConverter(player, MmoCoreWrapper())
    }

    private val playerRef = ExistingWeakReference(player)
    private val expCurve: ExpCurve
        get() = mmoCore.getExpCurve(playerRef.get())

    override fun getExpToReachLevel(level: Int): Double {
        return if (level <= 1) 0.0 else expCurve.getExperience(level).toDouble()
    }
}
