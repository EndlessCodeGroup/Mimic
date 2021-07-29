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

package ru.endlesscode.mimic.impl.mimic

import org.bukkit.entity.Player
import ru.endlesscode.mimic.classes.BukkitClassSystem
import java.util.*

/**
 * Class system based on permissions.
 *
 * To set user classes just give him permission like these:
 * - mimic.class.ClassOne
 * - mimic.class.ClassTwo
 * First class will be used as primary.
 */
public class PermissionsClassSystem private constructor(player: Player) : BukkitClassSystem(player) {

    public companion object {
        public const val ID: String = "permissions"
        public const val PERMISSION_PREFIX: String = "mimic.class."
    }

    override val classes: List<String>
        get() {
            return player.effectivePermissions.asSequence()
                .filter { it.value && it.permission.startsWith(PERMISSION_PREFIX) }
                .map { it.permission.substring(PERMISSION_PREFIX.length) }
                .toList()
        }

    override fun hasClass(requiredClass: String): Boolean {
        return player.hasPermission(PERMISSION_PREFIX + requiredClass.lowercase(Locale.getDefault()))
    }

    internal class Provider : BukkitClassSystem.Provider(ID) {
        override fun getSystem(player: Player): BukkitClassSystem = PermissionsClassSystem(player)
    }
}
