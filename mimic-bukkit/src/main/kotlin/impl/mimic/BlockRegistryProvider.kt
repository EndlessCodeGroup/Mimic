package ru.endlesscode.mimic.impl.mimic

import org.bukkit.plugin.RegisteredServiceProvider
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.blocks.BukkitBlockRegistry

@OptIn(ExperimentalMimicApi::class)
internal typealias BlockRegistryProvider = RegisteredServiceProvider<BukkitBlockRegistry>

@ExperimentalMimicApi
internal inline val BlockRegistryProvider.registry: BukkitBlockRegistry
    get() = provider
