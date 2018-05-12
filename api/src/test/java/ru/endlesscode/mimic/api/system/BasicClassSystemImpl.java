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

package ru.endlesscode.mimic.api.system;

import org.jetbrains.annotations.*;
import ru.endlesscode.mimic.api.system.registry.*;

import java.util.*;

/**
 * Basic implementation of class system.
 *
 * @author Osip Fatkullin
 * @since 1.0
 */
@Metadata(classes = {"ru.endlesscode.mimic.api.system.ClassSystem"})
public class BasicClassSystemImpl extends ClassSystem {
    public static final ClassSystem.Factory FACTORY = new ClassSystem.Factory(arg -> new BasicClassSystemImpl());

    @NotNull
    @Override
    public List<String> getClasses() {
        return new ArrayList<>();
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public @NotNull Object getHandler() {
        return new Object();
    }

    @NotNull
    @Override
    public String getName() {
        return "";
    }
}
