/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
 *
 * BukkitMimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BukkitMimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BukkitMimic.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic

import org.bukkit.entity.Player

/** [ProviderService] requiring [Player] to initialize "PlayerSystem" [T]. */
public abstract class PlayerSystemProviderService<T : Any> : ProviderService<T> {

    override val id: String

    @Deprecated(
        "Use constructor without parameters, override getId() if you want to use ID different from plugin name.",
        ReplaceWith("this()")
    )
    public constructor(id: String) {
        @Suppress("LeakingThis")
        this.id = id
    }

    @Suppress("DEPRECATION")
    public constructor() : this(MimicService.USE_PLUGIN_NAME_AS_ID)

    /**
     * Returns new instance of [T] is given [arg] is instance of [Player].
     * Otherwise, throws [ClassCastException].
     */
    @Deprecated("Use getSystem(player) instead", ReplaceWith("this.getSystem(arg)"))
    final override fun get(arg: Any): T = getSystem(arg as Player)

    /** Returns new instance of [T] initialized with the given [player] object. */
    public abstract fun getSystem(player: Player): T
}
