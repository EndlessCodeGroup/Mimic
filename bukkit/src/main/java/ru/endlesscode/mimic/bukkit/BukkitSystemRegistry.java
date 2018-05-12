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

import com.google.common.annotations.*;
import org.bukkit.plugin.*;
import org.jetbrains.annotations.*;
import ru.endlesscode.mimic.api.system.*;
import ru.endlesscode.mimic.api.system.registry.*;

/**
 * Implementation of system registry for bukkit.
 * Using {@link org.bukkit.plugin.ServicesManager}
 */
public class BukkitSystemRegistry extends SystemRegistry {
    private final Plugin plugin;
    private final ServicesManager servicesManager;

    BukkitSystemRegistry(Plugin plugin, ServicesManager servicesManager) {
        this.plugin = plugin;
        this.servicesManager = servicesManager;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    protected <FactoryT extends SystemFactory<? extends PlayerSystem>> void registerSystem(
            @NotNull Class<FactoryT> factoryClass,
            @NotNull FactoryT subsystemFactory,
            @NotNull MetadataAdapter meta) {
        ServicePriority priority = servicePriorityFromSystem(meta.getPriority());
        this.servicesManager.register(factoryClass, subsystemFactory, this.plugin, priority);
    }

    /**
     * {@inheritDoc}.
     */
    @VisibleForTesting
    static @NotNull ServicePriority servicePriorityFromSystem(@NotNull SystemPriority priority) {
        int priorityIndex = priority.ordinal();
        return ServicePriority.values()[priorityIndex];
    }

    /**
     * {@inheritDoc}.
     */
    @NotNull
    @Override
    public <SystemT extends PlayerSystem> SystemFactory<SystemT> getFactory(
            @NotNull Class<? extends SystemFactory<SystemT>> factoryClass)
            throws SystemNotFoundException {
        RegisteredServiceProvider<? extends SystemFactory<SystemT>> systemProvider
                = this.servicesManager.getRegistration(factoryClass);
        if (systemProvider == null) {
            throw new SystemNotFoundException(String.format("No one system '%s' found", factoryClass.getName()));
        }

        return systemProvider.getProvider();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void unregisterAllSubsystems() {
        servicesManager.unregisterAll(this.plugin);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public <SubsystemT extends PlayerSystem> void unregisterFactory(
            @NotNull SystemFactory<? extends SubsystemT> factory) {
        servicesManager.unregister(factory);
    }
}
