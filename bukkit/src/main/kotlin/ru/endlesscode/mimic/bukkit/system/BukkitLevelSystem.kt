/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2018 Osip Fatkullin
 * Copyright (C) 2018 EndlessCode Group and contributors
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
package ru.endlesscode.mimic.bukkit.system

import org.bukkit.entity.Player
import ru.endlesscode.mimic.api.ref.ExistingWeakReference
import ru.endlesscode.mimic.api.system.ExpLevelConverter
import ru.endlesscode.mimic.api.system.LevelSystem

/** [LevelSystem] adapted for Bukkit. */
abstract class BukkitLevelSystem internal constructor(
    override val converter: ExpLevelConverter,
    player: Player
) : LevelSystem {

    val player: Player get() = playerRef.get()

    private val playerRef: ExistingWeakReference<Player> = ExistingWeakReference(player)

    /** Factory of Bukkit level systems. */
    class Factory<SubsystemT : BukkitLevelSystem>(tag: String, constructor: (Player) -> SubsystemT) :
        LevelSystem.Factory<SubsystemT>(tag, { constructor(it as Player) })
}
