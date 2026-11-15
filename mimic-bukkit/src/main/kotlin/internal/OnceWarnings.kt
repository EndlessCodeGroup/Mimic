// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.internal

import java.util.concurrent.ConcurrentHashMap

/** Logs each warning once, identified by its key. Thread-safe. */
internal class OnceWarnings {

    /** Keys of the warnings already logged. */
    private val loggedKeys = ConcurrentHashMap.newKeySet<String>()

    /** Logs the warning with the given [key], if it wasn't logged yet. */
    fun warn(key: String, message: () -> String) {
        if (loggedKeys.add(key)) Log.w(message())
    }
}
