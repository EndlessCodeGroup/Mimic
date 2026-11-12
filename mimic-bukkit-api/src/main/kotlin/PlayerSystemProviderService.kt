// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic

import org.bukkit.entity.Player

/** [ProviderService] requiring [Player] to initialize "PlayerSystem" [T]. */
public fun interface PlayerSystemProviderService<T : Any> : ProviderService<T> {

    /**
     * Returns new instance of [T] is given [arg] is instance of [Player].
     * Otherwise, throws [ClassCastException].
     */
    @Deprecated("Use getSystem(player) instead", ReplaceWith("this.getSystem(arg)"))
    override fun get(arg: Any): T = getSystem(arg as Player)

    /** Returns new instance of [T] initialized with the given [player] object. */
    public fun getSystem(player: Player): T
}
