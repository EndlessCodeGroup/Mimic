// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // ClassSystem API is deprecated but still supported

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
@Deprecated("Classes API has no permissions-based classes, use a plugin providing classes instead.")
public class PermissionsClassSystem private constructor(player: Player) : BukkitClassSystem(player) {

    override val classes: List<String>
        get() {
            return player.effectivePermissions.asSequence()
                .filter { it.value && it.permission.startsWith(PERMISSION_PREFIX) }
                .map { it.permission.substring(PERMISSION_PREFIX.length) }
                .toList()
        }

    override fun hasClass(requiredClass: String): Boolean {
        val permission = PERMISSION_PREFIX + requiredClass.lowercase(Locale.getDefault())
        // Check that permission is set explicitly, otherwise all classes are available to operators
        return player.isPermissionSet(permission) && player.hasPermission(permission)
    }

    internal class Provider : BukkitClassSystem.Provider {
        override val id: String = ID
        override fun getSystem(player: Player): BukkitClassSystem = PermissionsClassSystem(player)
    }

    public companion object {
        public const val ID: String = "permissions"
        public const val PERMISSION_PREFIX: String = "mimic.class."
    }
}
