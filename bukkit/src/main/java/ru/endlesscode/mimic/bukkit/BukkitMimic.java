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

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageKeys;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.endlesscode.mimic.api.system.ClassSystem;
import ru.endlesscode.mimic.api.system.LevelSystem;
import ru.endlesscode.mimic.api.system.PlayerSystem;
import ru.endlesscode.mimic.api.system.registry.SystemNotNeededException;
import ru.endlesscode.mimic.api.system.registry.SystemNotRegisteredException;
import ru.endlesscode.mimic.bukkit.command.ClassSystemSubcommand;
import ru.endlesscode.mimic.bukkit.command.CommandUtil;
import ru.endlesscode.mimic.bukkit.command.LevelSystemSubcommand;
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

    @Override
    public void onEnable() {
        registerCommands();
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
            Log.d(String.format("Subsystem '%s' not registered: %s", system.getSimpleName(), e.getMessage()));
        }
    }

    private void registerCommands() {
        BukkitCommandManager manager = new BukkitCommandManager(this);
        //noinspection deprecation
        manager.enableUnstableAPI("help");
        manager.getCommandReplacements().addReplacements(
                "command", "mimic|bmimic|bukkitmimic",
                "perm", "mimic.admin"
        );
        manager.setDefaultExceptionHandler((command, registeredCommand, sender, args, t) -> {
            if (t instanceof IllegalArgumentException || t instanceof UnsupportedOperationException) {
                sender.sendError(MessageKeys.ERROR_PREFIX, "{message}", t.getMessage());
                return false;
            }

            return false;
        });

        CommandUtil util = new CommandUtil();
        manager.registerCommand(new MimicCommand(util));
        manager.registerCommand(new LevelSystemSubcommand(systemRegistry.getSystemFactory(LevelSystem.class), util));
        manager.registerCommand(new ClassSystemSubcommand(systemRegistry.getSystemFactory(ClassSystem.class), util));
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
