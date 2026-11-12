// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mimic

import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.ServiceEntry
import ru.endlesscode.mimic.blocks.BukkitBlockRegistry

@OptIn(ExperimentalMimicApi::class)
internal typealias BlockRegistryProvider = ServiceEntry<BukkitBlockRegistry>

@ExperimentalMimicApi
internal inline val BlockRegistryProvider.registry: BukkitBlockRegistry
    get() = service
