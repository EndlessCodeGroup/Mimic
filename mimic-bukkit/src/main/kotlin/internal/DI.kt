// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.internal

import kotlinx.serialization.hocon.Hocon

/** Dependencies available for the whole plugin. */
internal object DI {

    val hocon: Hocon by lazy {
        Hocon {
            useConfigNamingConvention = true
        }
    }
}
