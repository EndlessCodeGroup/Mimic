package ru.endlesscode.mimic.internal

import org.bukkit.NamespacedKey

internal fun namespacedKeyOf(key: String): NamespacedKey? = NamespacedKey.fromString(key)
