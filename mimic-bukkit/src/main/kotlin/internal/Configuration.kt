package ru.endlesscode.mimic.internal

import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.FileConfigurationOptions

internal fun FileConfigurationOptions.setHeader(vararg lines: String) {
    setHeader(lines.asList())
}

internal fun Configuration.applyDefaults() {
    val defaults = defaults ?: return
    for (key in defaults.getKeys(true)) set(key, get(key))
}

internal fun Configuration.setComments(path: String, vararg comments: String?) {
    setComments(path, comments.asList())
}
