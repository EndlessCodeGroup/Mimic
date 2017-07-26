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

package ru.endlesscode.mimic.api.ref;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

/**
 * Weak reference that can throw {@code IllegalStateException} if referent
 * objects not exists.
 *
 * @author Osip Fatkullin
 * @version 1.0
 */
public class ExistingWeakReference<T> extends WeakReference<T> {
    /**
     * Creates a new weak reference that refers to the given object.  The new
     * reference is not registered with any queue. If referent is null {@link #get()}
     * will throw IllegalStateException.
     *
     * @param referent object the new weak reference will refer to
     */
    public ExistingWeakReference(T referent) {
        super(referent);
    }

    /**
     * {@inheritDoc}.
     *
     * @throws IllegalStateException If referent object already not exists
     */
    @Override
    public @NotNull T get() {
        T referent = super.get();
        if (referent == null) {
            throw new IllegalStateException("Referent object is null");
        }

        return referent;
    }
}