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

import co.aikar.commands.CommandCompletionContext
import co.aikar.commands.CommandCompletions

internal inline fun <reified T : Enum<T>> CommandCompletions<*>.registerEnumCompletion(
    id: String = T::class.java.simpleName.lowercase()
): CommandCompletions.CommandCompletionHandler<out CommandCompletionContext<*>>? {
    return registerEnumCompletion(id, enumValues<T>())
}

internal fun <T : Enum<T>> CommandCompletions<*>.registerEnumCompletion(
    id: String,
    values: Array<T>
): CommandCompletions.CommandCompletionHandler<out CommandCompletionContext<*>>? {
    return registerStaticCompletion(id, values.map { it.name.lowercase() })
}
