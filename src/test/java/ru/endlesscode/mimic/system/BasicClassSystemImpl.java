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

package ru.endlesscode.mimic.system;

import org.jetbrains.annotations.NotNull;
import ru.endlesscode.mimic.system.registry.Metadata;

import java.util.Arrays;
import java.util.List;

/**
 * Basic implementation of class system
 *
 * @author Osip Fatkullin
 * @since 1.0
 */
@Metadata
public class BasicClassSystemImpl extends ClassSystem {
    private List<String> classes;

    @NotNull
    @Override
    public List<String> getClasses() {
        return classes;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Initializes and returns copy of current system
     *
     * @param args Args for initialization
     * @return Initialized system copy
     */
    @Override
    public @NotNull BasicClassSystemImpl initializedCopy(Object... args) throws CloneNotSupportedException {
        BasicClassSystemImpl copy = this.clone();
        copy.setClasses((String[]) args);
        return copy;
    }

    /**
     * Returns player-related object
     *
     * @return Player-related object
     */
    @Override
    public @NotNull Object getHandler() {
        return this;
    }

    @Override
    protected BasicClassSystemImpl clone() throws CloneNotSupportedException {
        return (BasicClassSystemImpl) super.clone();
    }

    @Override
    public String getName() {
        return "Basic Class System";
    }

    public void setClasses(String... classes) {
        this.classes = Arrays.asList(classes);
    }
}
