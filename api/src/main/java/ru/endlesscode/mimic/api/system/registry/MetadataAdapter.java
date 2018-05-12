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

package ru.endlesscode.mimic.api.system.registry;

import org.jetbrains.annotations.*;
import ru.endlesscode.mimic.api.system.*;

/**
 * Adapter to work with systems {@code Metadata}.
 *
 * @see Metadata
 * @param <SubsystemT>  Subsystem type
 * @author Osip Fatkullin
 * @since 1.0
 */
public class MetadataAdapter<SubsystemT extends PlayerSystem> {
    private final Metadata meta;

    /**
     * Gets metadata from class annotation. If annotation not exists - throws exception.
     *
     * @see Metadata
     * @param <SubsystemT>  Subsystem type
     * @param theClass      Subsystem class
     * @return {@code MetadataAdapter} if metadata exists, otherwise throws exception
     * @throws IllegalArgumentException If {@code Metadata} not exists
     */
    @NotNull
    public static <SubsystemT extends PlayerSystem> MetadataAdapter<SubsystemT> getNotNullMeta(
            @NotNull Class<SubsystemT> theClass) {
        Metadata meta = theClass.getAnnotation(Metadata.class);
        if (meta == null) {
            throw new IllegalArgumentException("Class not contains metadata.");
        }

        return new MetadataAdapter<>(meta, theClass);
    }

    /**
     * Creates {@code MetadataAdapter}.
     *
     * @param meta The metadata
     */
    protected MetadataAdapter(@NotNull Metadata meta, Class<SubsystemT> subsystemClass) {
        this.meta = meta;
    }

    /**
     * Checks existence of all required classes.
     *
     * @return {@code true} if all classes exists, otherwise {@code false}
     */
    public boolean requiredClassesExists() {
        String[] classNames = meta.classes();

        try {
            for (String className : classNames) {
                Class.forName(className);
            }

            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Returns system priority.
     *
     * @return System priority
     */
    public SystemPriority getPriority() {
        return meta.priority();
    }
}
