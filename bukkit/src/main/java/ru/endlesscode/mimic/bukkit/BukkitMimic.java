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

package ru.endlesscode.mimic.bukkit;

import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.endlesscode.mimic.api.system.PlayerSystem;
import ru.endlesscode.mimic.api.system.registry.SystemNotNeededException;
import ru.endlesscode.mimic.api.system.registry.SystemNotRegisteredException;
import ru.endlesscode.mimic.bukkit.command.MimicCommand;
import ru.endlesscode.mimic.bukkit.system.PermissionsClassSystem;
import ru.endlesscode.mimic.bukkit.system.SkillApiClassSystem;
import ru.endlesscode.mimic.bukkit.system.VanillaLevelSystem;
import ru.endlesscode.mimic.bukkit.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Main class of the plugin.
 */
public class BukkitMimic extends JavaPlugin {
    private static final boolean DEBUG = true;

    private static BukkitMimic instance;
    private static Logger logger;

    private BukkitSystemRegistry systemRegistry;

    /**
     * All default subsystems.
     */
    private final List<Class<? extends PlayerSystem>> defaultSubsystems = Arrays.asList(
            VanillaLevelSystem.class, PermissionsClassSystem.class, SkillApiClassSystem.class
    );

    @Override
    public void onLoad() {
        instance = this;

        Log.wrap(this.getLogger(), DEBUG);

        this.initRegistry();
        this.hookDefaultSystems();
    }

    private void initRegistry() {
        ServicesManager sm = this.getServer().getServicesManager();
        this.systemRegistry = new BukkitSystemRegistry(this, sm);
    }

    private void hookDefaultSystems() {
        defaultSubsystems.forEach(this::hookSystem);
    }

    private <T extends PlayerSystem> void hookSystem(Class<? extends T> system) {
        try {
            this.systemRegistry.registerSubsystem(system);
            Log.d(String.format("Subsystem '%s' registered.", system.getSimpleName()));
        } catch (SystemNotRegisteredException e) {
            logger.warning(system.getSimpleName() + ": " + e.getMessage());
            Log.d(e);
        } catch (SystemNotNeededException e) {
            Log.d(e);
        }
    }

    /**
     * Write a message to log if debug is enabled.
     *
     * @param message Debug message
     */
    private void debug(String message) {
        if (DEBUG) {
            logger.warning("[DEBUG] " + message);
        }
    }

    /**
     * Write an exception to log if debug is enabled.
     *
     * @param throwable Thrown exception
     */
    private void debug(Throwable throwable) {
        if (DEBUG) {
            logger.log(Level.WARNING, "[DEBUG] Yay! Long-awaited exception!", throwable);
        }
    }

    @Override
    public void onDisable() {
        this.systemRegistry.unregisterAllSubsystems();
    }

    /**
     * Returns system registry.
     *
     * @return Mimic system registry
     */
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static BukkitSystemRegistry getSystemRegistry() {
        return instance.systemRegistry;
    }
}
