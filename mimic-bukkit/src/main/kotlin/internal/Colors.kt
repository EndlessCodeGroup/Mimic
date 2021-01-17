package ru.endlesscode.mimic.internal

import org.bukkit.ChatColor

internal fun String.stripColor(): String {
    return checkNotNull(ChatColor.stripColor(this))
}
