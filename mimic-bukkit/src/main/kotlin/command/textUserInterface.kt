/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
 *
 * BukkitMimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BukkitMimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BukkitMimic.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.endlesscode.mimic.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine

internal fun successText(text: String) = Component.text(text, NamedTextColor.GOLD)
internal fun errorText(text: String) = Component.text(text, NamedTextColor.RED)

internal fun TextComponent.Builder.appendStats(vararg stats: Pair<String, String>) {
    for ((key, value) in stats) {
        append("$key: ", NamedTextColor.DARK_AQUA)
        append(value, NamedTextColor.GRAY)
        appendLine()
    }
}
