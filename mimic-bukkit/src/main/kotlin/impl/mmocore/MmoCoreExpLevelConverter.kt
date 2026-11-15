// Copyright (c) 2021 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // LevelSystem API is deprecated but still supported

package ru.endlesscode.mimic.impl.mmocore

import net.Indyuce.mmocore.experience.curve.ExperienceCurve
import org.bukkit.OfflinePlayer
import ru.endlesscode.mimic.level.ExpLevelConverter
import ru.endlesscode.mimic.util.ExistingWeakReference

@Deprecated("Use Levels API instead, it provides experience to the next MMOCore level.")
public class MmoCoreExpLevelConverter internal constructor(
    player: OfflinePlayer,
    private val mmoCore: MmoCoreWrapper,
) : ExpLevelConverter {

    public companion object {
        @JvmStatic
        public fun get(player: OfflinePlayer): ExpLevelConverter = MmoCoreExpLevelConverter(player, MmoCoreWrapper())
    }

    private val playerRef = ExistingWeakReference(player)
    private val expCurve: ExperienceCurve
        get() = mmoCore.getExpCurve(playerRef.get())

    // MMOCore curves return experience to reach the next level, from `level` to `level + 1`
    override fun getExpToReachLevel(level: Int): Double {
        return if (level <= 1) 0.0 else expCurve.getExperience(mmoCore.getData(playerRef.get()), level - 1).toDouble()
    }
}
