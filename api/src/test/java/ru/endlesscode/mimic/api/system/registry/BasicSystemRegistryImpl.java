/*
 * This file is part of MimicAPI.
 * Copyright (C) 2018 Osip Fatkullin
 * Copyright (C) 2018 EndlessCode Group and contributors
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

package ru.endlesscode.mimic.api.system.registry;

import org.jetbrains.annotations.*;
import ru.endlesscode.mimic.api.system.*;

/**
 * @author Osip Fatkullin
 * @since 1.0
 */
public class BasicSystemRegistryImpl extends SystemRegistry {
    @Override
    public <SystemT extends PlayerSystem> @NotNull SystemFactory<SystemT> getFactory(
            @NotNull Class<? extends SystemFactory<SystemT>> systemFactoryClass) {
        return new SystemFactory<>(arg -> null);
    }


    @Override
    protected <FactoryT extends SystemFactory<? extends PlayerSystem>> void registerSystem(
            @NotNull Class<FactoryT> factoryClass, @NotNull
            FactoryT subsystemFactory,
            @NotNull MetadataAdapter meta) {

    }

    @Override
    public void unregisterAllSubsystems() {}

    @Override
    public <SubsystemT extends PlayerSystem> void unregisterFactory(@NotNull SystemFactory<? extends SubsystemT> factory) {}
}