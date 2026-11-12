// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.level

import org.bukkit.entity.Player
import ru.endlesscode.mimic.PlayerSystemProviderService
import ru.endlesscode.mimic.util.ExistingWeakReference

/** [LevelSystem] for Bukkit. */
public abstract class BukkitLevelSystem(player: Player) : LevelSystem {

    /** Player owning this level system. */
    public val player: Player get() = playerRef.get()

    private val playerRef: ExistingWeakReference<Player> = ExistingWeakReference(player)

    /** Provider of Bukkit level systems. */
    public fun interface Provider : PlayerSystemProviderService<BukkitLevelSystem>
}
