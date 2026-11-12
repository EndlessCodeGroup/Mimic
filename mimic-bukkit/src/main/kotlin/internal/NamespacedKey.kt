// Copyright (c) 2022 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.internal

import org.bukkit.NamespacedKey

internal fun namespacedKeyOf(key: String): NamespacedKey? = NamespacedKey.fromString(key)
