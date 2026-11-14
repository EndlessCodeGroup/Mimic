// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // LevelSystem API is deprecated but still supported

package ru.endlesscode.mimic.level

import org.bukkit.entity.Player
import ru.endlesscode.mimic.PlayerSystemProviderService
import ru.endlesscode.mimic.util.ExistingWeakReference

/** [LevelSystem] for Bukkit. */
@Deprecated("Use Levels API instead, it supports several levels per player.")
public abstract class BukkitLevelSystem(player: Player) : LevelSystem {

    /** Player owning this level system. */
    public val player: Player get() = playerRef.get()

    private val playerRef: ExistingWeakReference<Player> = ExistingWeakReference(player)

    /**
     * Provider of Bukkit level systems.
     * It is also available via [Levels] as the single track `<id>:level`,
     * unless a [LevelProvider] with the same ID is registered.
     */
    @Deprecated("Use LevelProvider instead.")
    public fun interface Provider : PlayerSystemProviderService<BukkitLevelSystem>
}
