package ru.endlesscode.mimic.internal

import org.bukkit.NamespacedKey

internal fun namespacedKeyOf(key: String): NamespacedKey? {
    return callCompat(
        "NamespacedKey.fromString",
        block = { NamespacedKey.fromString(key) },
        compat = { namespacedKeyCompat(key) },
    )
}

@Suppress("DEPRECATION")
private fun namespacedKeyCompat(string: String): NamespacedKey? {
    val components = string.split(":", limit = 3)
    return when (components.size) {
        1 -> NamespacedKey.minecraft(components.first())
        2 -> NamespacedKey(components.first(), components.last())
        else -> null
    }
}
