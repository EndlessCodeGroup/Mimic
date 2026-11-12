// Copyright (c) 2017 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:JvmName("Requirements")

package ru.endlesscode.mimic.util

/** Checks that the given [classes] are existing. */
public fun checkClassesLoaded(vararg classes: String): Boolean {
    return try {
        classes.forEach { Class.forName(it) }
        true
    } catch (_: ClassNotFoundException) {
        false
    }
}
