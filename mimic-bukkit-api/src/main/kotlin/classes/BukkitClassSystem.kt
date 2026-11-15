// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // ClassSystem API is deprecated but still supported

package ru.endlesscode.mimic.classes

import org.bukkit.entity.Player
import ru.endlesscode.mimic.PlayerSystemProviderService
import ru.endlesscode.mimic.util.ExistingWeakReference

/** [ClassSystem] for Bukkit. */
@Deprecated("Use Classes API instead, it supports class slots, subclasses and class levels.")
public abstract class BukkitClassSystem(player: Player) : ClassSystem {

    /** Player owning this class system. */
    public val player: Player get() = playerRef.get()

    private val playerRef: ExistingWeakReference<Player> = ExistingWeakReference(player)

    /** Provider of Bukkit class systems. */
    @Deprecated("Use ClassProvider instead.")
    public fun interface Provider : PlayerSystemProviderService<BukkitClassSystem>
}
