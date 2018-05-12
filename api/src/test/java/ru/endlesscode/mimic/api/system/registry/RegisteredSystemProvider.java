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

import org.jetbrains.annotations.NotNull;
import ru.endlesscode.mimic.api.system.PlayerSystem;

/**
 * A registered system provider.
 *
 * @param <SubsystemT> Service
 * @author Osip Fatkullin
 * @since 1.0
 */
public class RegisteredSystemProvider<SubsystemT extends PlayerSystem> implements Comparable<RegisteredSystemProvider<?>> {
    private final Class<?> system;
    private final SubsystemT provider;
    private final SystemPriority priority;

    public RegisteredSystemProvider(Class<?> system, SubsystemT provider, SystemPriority priority) {
        this.system = system;
        this.provider = provider;
        this.priority = priority;
    }

    public Class<?> getSystem() {
        return system;
    }

    public SubsystemT getProvider() {
        return provider;
    }

    public SystemPriority getPriority() {
        return priority;
    }

    @Override
    public int compareTo(@NotNull RegisteredSystemProvider<?> other) {
        if (priority.ordinal() == other.getPriority().ordinal()) {
            return 0;
        } else {
            return priority.ordinal() < other.getPriority().ordinal() ? 1 : -1;
        }
    }
}
