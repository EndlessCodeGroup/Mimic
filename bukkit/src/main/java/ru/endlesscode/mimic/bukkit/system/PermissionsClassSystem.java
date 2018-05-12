/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2017 EndlessCode Group and contributors
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

package ru.endlesscode.mimic.bukkit.system;

import com.google.common.annotations.*;
import org.bukkit.entity.*;
import org.bukkit.permissions.*;
import org.jetbrains.annotations.*;
import ru.endlesscode.mimic.api.system.registry.*;

import java.util.*;

/**
 * Class system based on permissions.
 *
 * <p>To set user classes just give him permission like these:
 *      - mimic.class.ClassOne
 *      - mimic.class.ClassTwo
 * First of classes will be primary.
 */
@Metadata(priority = SystemPriority.LOWEST)
public class PermissionsClassSystem extends BukkitClassSystem {
    public static final Factory FACTORY = new Factory(playerObj -> new PermissionsClassSystem((Player) playerObj));

    @VisibleForTesting
    static final String PERMISSION_PREFIX = "mimic.class.";

    private PermissionsClassSystem(@NotNull Player player) {
        super(player);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public boolean hasRequiredClass(@NotNull String requiredClass) {
        Player player = this.playerRef.get();
        return player.hasPermission(PERMISSION_PREFIX + requiredClass.toLowerCase());
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public @NotNull List<String> getClasses() {
        List<String> matchedPermissions = new ArrayList<>();
        Player player = this.playerRef.get();
        Set<PermissionAttachmentInfo> perms = player.getEffectivePermissions();
        for (PermissionAttachmentInfo perm : perms) {
            boolean positive = perm.getValue();
            String permission = perm.getPermission();
            if (positive && permission.startsWith(PERMISSION_PREFIX)) {
                String theClass = permission.substring(PERMISSION_PREFIX.length());
                matchedPermissions.add(theClass);
            }
        }

        return matchedPermissions;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public String getName() {
        return "Permission Class System";
    }
}
