package ru.endlesscode.mimic.internal

import org.bukkit.configuration.Configuration

internal fun Configuration.applyDefaults() {
    val defaults = defaults ?: return
    for (key in defaults.getKeys(true)) set(key, get(key))
}

internal fun Configuration.setComments(path: String, vararg comments: String) {
    setComments(path, comments.toList())
}
