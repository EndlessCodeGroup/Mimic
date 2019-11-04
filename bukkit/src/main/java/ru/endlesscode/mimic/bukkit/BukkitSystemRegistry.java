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

import com.google.common.annotations.VisibleForTesting;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.jetbrains.annotations.NotNull;
import ru.endlesscode.mimic.api.system.PlayerSystem;
import ru.endlesscode.mimic.api.system.SystemFactory;
import ru.endlesscode.mimic.api.system.registry.SubsystemPriority;
import ru.endlesscode.mimic.api.system.registry.SystemRegistry;

/**
 * Implementation of system registry for bukkit.
 * Using {@link org.bukkit.plugin.ServicesManager}
 */
public class BukkitSystemRegistry implements SystemRegistry {
    private final Plugin plugin;
    private final ServicesManager servicesManager;

    @VisibleForTesting
    static @NotNull ServicePriority servicePriorityFromSystem(@NotNull SubsystemPriority priority) {
        int priorityIndex = priority.ordinal();
        return ServicePriority.values()[priorityIndex];
    }

    BukkitSystemRegistry(Plugin plugin, ServicesManager servicesManager) {
        this.plugin = plugin;
        this.servicesManager = servicesManager;
    }

    @Override
    public <SystemT extends PlayerSystem, FactoryT extends SystemFactory<? extends SystemT>> void registerSystem(
            @NotNull Class<FactoryT> factoryClass,
            @NotNull FactoryT subsystemFactory,
            @NotNull SubsystemPriority priority
    ) {
        ServicePriority servicePriority = servicePriorityFromSystem(priority);
        this.servicesManager.register(factoryClass, subsystemFactory, this.plugin, servicePriority);
    }

    @Override
    public <SystemT extends PlayerSystem> SystemFactory<SystemT> getFactory(
            @NotNull Class<? extends SystemFactory<SystemT>> factoryClass
    ) {
        return this.servicesManager.load(factoryClass);
    }

    @Override
    public void unregisterAllSubsystems() {
        servicesManager.unregisterAll(this.plugin);
    }

    @Override
    public <SystemT extends PlayerSystem> void unregisterFactory(@NotNull SystemFactory<? extends SystemT> factory) {
        servicesManager.unregister(factory);
    }
}
