// Copyright (c) 2022 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.internal

private val notSupportedCalls = mutableSetOf<String>()

@Suppress("unused") // Reserved for further compatibility calls
internal inline fun <T> callCompat(
    key: String,
    block: () -> T,
    compat: () -> T,
): T {
    if (key !in notSupportedCalls) {
        try {
            return block()
        } catch (_: IncompatibleClassChangeError) {
            notSupportedCalls.add(key)
        }
    }

    return compat()
}
