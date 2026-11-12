// Copyright (c) 2017 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.util

import java.lang.ref.WeakReference

/**
 * Weak reference that throws [IllegalStateException] if referent objects not exists.
 *
 * @param referent object the new weak reference will refer to
 * @constructor Creates a new weak reference that refers to the given object. The new
 * reference is not registered with any queue.
 */
public class ExistingWeakReference<T : Any>(referent: T) : WeakReference<T>(referent) {

    /** Throws [IllegalStateException] if reference object has been cleared. */
    override fun get(): T = checkNotNull(super.get()) { "Referent object is null." }
}
