/*
 * This file is part of MimicAPI.
 * Copyright (C) 2017 Osip Fatkullin
 * Copyright (C) 2017 EndlessCode Group and Contributors
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
import ru.endlesscode.mimic.system.PlayerSystem;
import ru.endlesscode.mimic.system.SystemFactory;

/**
 * @author Osip Fatkullin
 * @since 1.0
 */
public class BasicSystemRegistryImpl extends SystemRegistry {
    @Override
    protected <FactoryT extends SystemFactory> void registerSystem(
            @NotNull Class<FactoryT> factoryClass,
            @NotNull FactoryT subsystemFactory,
            @NotNull MetadataAdapter meta) {}

    @NotNull
    @Override
    public <SystemT extends PlayerSystem> SystemFactory<SystemT> getSystem(
            @NotNull Class<SystemT> systemTypeClass)
            throws SystemNotFoundException {
        return arg -> null;
    }

    @Override
    public void unregisterAllSubsystems() {}

    @Override
    public <FactoryT extends SystemFactory> void unregisterSubsystem(@NotNull FactoryT factory) {}
}
