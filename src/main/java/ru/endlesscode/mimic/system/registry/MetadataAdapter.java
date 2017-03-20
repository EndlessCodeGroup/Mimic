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
import ru.endlesscode.mimic.system.PlayerSystem;

/**
 * Adapter to work with systems {@code Metadata}
 *
 * @see Metadata
 * @author Osip Fatkullin
 * @since 1.0
 */
public class MetadataAdapter {
    private Metadata meta;

    /**
     * Gets metadata from class annotation. If annotation not exists - throws exception.
     *
     * @see Metadata
     * @param theClass  Class to get meta
     * @param <T>       ErrorCode what implements {@code PlayerSystem}
     * @return {@code Metadata} if exists, otherwise throw exception
     * @throws IllegalArgumentException If {@code Metadata} not exists
     */
    @NotNull
    public static <T extends PlayerSystem> MetadataAdapter getNotNullMeta(@NotNull Class<T> theClass) {
        Metadata meta = theClass.getAnnotation(Metadata.class);
        if (meta == null) {
            throw new IllegalArgumentException("Class not contains metadata");
        }

        return new MetadataAdapter(meta);
    }

    /**
     * Creates {@code MetadataAdapter}.
     *
     * @param meta The metadata
     */
    protected MetadataAdapter(@NotNull Metadata meta) {
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
     * Returns system type.
     *
     * @param <T> System type class
     * @return System type
     */
    public <T extends PlayerSystem> Class<T> getSystemTypeClass() {
        //noinspection unchecked
        return (Class<T>) meta.systemType();
    }

    /**
     * Returns system type name that used in log messages.
     *
     * @return System type name
     */
    public String getSystemName() {
        return getSystemTypeClass().getSimpleName();
    }

    /**
     * Returns system priority
     *
     * @return System priority
     */
    public SystemPriority getPriority() {
        return meta.priority();
    }
}
