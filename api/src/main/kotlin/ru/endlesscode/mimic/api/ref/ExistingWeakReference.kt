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

package ru.endlesscode.mimic.api.ref

import org.jetbrains.annotations.NotNull
import java.lang.ref.WeakReference

/**
 * Weak reference that throws [IllegalStateException] if referent objects not exists.
 *
 * @param referent object the new weak reference will refer to
 * @constructor Creates a new weak reference that refers to the given object. The new
 * reference is not registered with any queue.
 *
 * @author Osip Fatkullin
 * @since 1.0
 */
class ExistingWeakReference<T : Any>(referent: T) : WeakReference<T>(referent) {

    /**
     * @throws IllegalStateException If referent object not exists
     */
    @NotNull
    override fun get(): T {
        return super.get() ?: error("Referent object is null.")
    }
}
