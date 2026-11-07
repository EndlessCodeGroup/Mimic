package ru.endlesscode.mimic.internal

import org.bukkit.NamespacedKey
import org.bukkit.plugin.Plugin

internal fun namespacedKeyOf(key: String): NamespacedKey? = NamespacedKey.fromString(key)

internal fun Plugin.parseKey(key: String): NamespacedKey? = NamespacedKey.fromString(key, this)
