// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:JvmSynthetic

package ru.endlesscode.mimic.bukkit

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.ServicesManager

/** Kotlin-style call of [ServicesManager.register]. */
public inline fun <reified T : Any> ServicesManager.register(
    provider: T,
    plugin: Plugin,
    priority: ServicePriority = ServicePriority.Normal,
) {
    register(T::class.java, provider, plugin, priority)
}

/** Kotlin-style call of [ServicesManager.load]. */
public inline fun <reified T : Any> ServicesManager.load(): T? {
    return load(T::class.java)
}

/** Kotlin-style call of [ServicesManager.getRegistrations] followed with [RegisteredServiceProvider.getProvider]. */
public inline fun <reified T : Any> ServicesManager.loadAll(): Collection<T> {
    return getRegistrations(T::class.java).map { it.provider }
}

/** Kotlin-style call of [ServicesManager.getRegistrations]. */
public inline fun <reified T : Any> ServicesManager.getRegistrations(): Collection<RegisteredServiceProvider<T>> {
    return getRegistrations(T::class.java)
}
