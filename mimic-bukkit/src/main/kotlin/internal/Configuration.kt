package ru.endlesscode.mimic.internal

import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.FileConfigurationOptions

@Suppress("DEPRECATION") // Changed in 1.18.1
internal fun FileConfigurationOptions.setHeader(vararg lines: String) {
    callCompat(
        "FileConfigurationOptions.setHeader",
        block =  { setHeader(lines.asList()) },
        compat = { header(lines.joinToString("\n")) },
    )
}

internal fun Configuration.applyDefaults() {
    val defaults = defaults ?: return
    for (key in defaults.getKeys(true)) set(key, get(key))
}

// Added in 1.18.1
internal fun Configuration.setComments(path: String, vararg comments: String?) {
    callCompat(
        "Configuration.setComments",
        block = { setComments(path, comments.asList()) },
        compat = { /* no-op */ },
    )
}
