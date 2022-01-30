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

package ru.endlesscode.mimic.classes

import org.bukkit.entity.Player
import ru.endlesscode.mimic.PlayerSystemProviderService
import ru.endlesscode.mimic.util.ExistingWeakReference

/** [ClassSystem] for Bukkit. */
public abstract class BukkitClassSystem(player: Player) : ClassSystem {

    /** Player owning this class system. */
    public val player: Player get() = playerRef.get()

    private val playerRef: ExistingWeakReference<Player> = ExistingWeakReference(player)

    /** Provider of Bukkit class systems. */
    public abstract class Provider : PlayerSystemProviderService<BukkitClassSystem> {
        @Suppress("DEPRECATION")
        @Deprecated(
            "Use constructor without parameters, override getId() if you want to use ID different from plugin name.",
            ReplaceWith("BukkitClassSystem.Provider()")
        )
        public constructor(id: String) : super(id)

        public constructor() : super()
    }
}
