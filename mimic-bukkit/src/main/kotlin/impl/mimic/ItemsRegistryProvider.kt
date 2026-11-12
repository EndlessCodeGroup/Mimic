// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mimic

import ru.endlesscode.mimic.ServiceEntry
import ru.endlesscode.mimic.items.BukkitItemsRegistry

internal typealias ItemsRegistryProvider = ServiceEntry<BukkitItemsRegistry>

internal inline val ItemsRegistryProvider.registry: BukkitItemsRegistry
    get() = service
