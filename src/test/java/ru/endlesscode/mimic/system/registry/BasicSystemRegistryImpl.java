/*
 * This file is part of MimicAPI.
 * Copyright (C) 2017 Osip Fatkullin
 *
 * MimicAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MimicAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MimicAPI.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.system.registry;

import org.jetbrains.annotations.NotNull;
import ru.endlesscode.mimic.system.BasicClassSystemImpl;
import ru.endlesscode.mimic.system.PlayerSystem;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author Osip Fatkullin
 * @since 1.0
 */
public class BasicSystemRegistryImpl extends SystemRegistry {
    /**
     * Map of providers.
     */
    private final Map<Class<?>, List<RegisteredSystemProvider<?>>> providers = new HashMap<>();

    /**
     * Constructor with logger initialization
     */
    protected BasicSystemRegistryImpl() {
        super(Logger.getLogger("BasicSystemRegistry"));
    }

    @Override
    protected <SubsystemT extends PlayerSystem> void registerSystem(
            @NotNull SubsystemT subsystem,
            @NotNull MetadataAdapter meta) {
        Class<?> system = meta.getSystemClass();
        SystemPriority priority = meta.getPriority();
        RegisteredSystemProvider<SubsystemT> registeredProvider = new RegisteredSystemProvider<>(system, subsystem, priority);

        this.insertProvider(registeredProvider);
    }

    private <SubsystemT extends PlayerSystem> void insertProvider(
            RegisteredSystemProvider<SubsystemT> registeredProvider) {
        Class<?> system = registeredProvider.getSystem();
        List<RegisteredSystemProvider<?>> registered = providers.get(system);
        if (registered == null) {
            registered = new ArrayList<>();
            providers.put(system, registered);
        }

        int position = Collections.binarySearch(registered, registeredProvider);
        if (position < 0) {
            registered.add(-(position + 1), registeredProvider);
        } else {
            registered.add(position, registeredProvider);
        }
    }

    @NotNull
    @Override
    protected <SystemT extends PlayerSystem> SystemT getSystem(@NotNull Class<SystemT> systemTypeClass, Object... args)
            throws SystemNotRegisteredException {
        List<RegisteredSystemProvider<?>> registered = providers.get(systemTypeClass);

        if (registered == null) {
            throw new SystemNotRegisteredException(String.format("System '%s' not found", systemTypeClass.getSimpleName()));
        }

        return systemTypeClass.cast(registered.get(0).getProvider());
    }

    @Override
    public void unregisterAllSubsystems() {
        providers.clear();
    }

    @Override
    public <SubsystemT extends PlayerSystem> void unregisterSubsystem(@NotNull SubsystemT subsystem) {
        Iterator<Map.Entry<Class<?>, List<RegisteredSystemProvider<?>>>> it = providers.entrySet().iterator();

        try {
            while (it.hasNext()) {
                Map.Entry<Class<?>, List<RegisteredSystemProvider<?>>> entry = it.next();
                Iterator<RegisteredSystemProvider<?>> it2 = entry.getValue().iterator();

                try {
                    // Removed entries that are from this plugin
                    while (it2.hasNext()) {
                        RegisteredSystemProvider<?> registered = it2.next();

                        if (registered.getProvider().equals(subsystem)) {
                            it2.remove();
                        }
                    }
                } catch (NoSuchElementException ignored) {}

                // Get rid of the empty list
                if (entry.getValue().size() == 0) {
                    it.remove();
                }
            }
        } catch (NoSuchElementException ignored) {}
    }

    @NotNull
    @Override
    protected <SubsystemT extends PlayerSystem> SubsystemT createSubsystemInstance(
            @NotNull Class<? extends SubsystemT> subsystemClass) {
        try {
            if (subsystemClass == BasicClassSystemImpl.class) {
                return subsystemClass.getConstructor(String[].class).newInstance((Object) new String[] {"ClassOne", "ClassTwo"});
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Instance from given class can't be created");
        }

        return super.createSubsystemInstance(subsystemClass);
    }
}
