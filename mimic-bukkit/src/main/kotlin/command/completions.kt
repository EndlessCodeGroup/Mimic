package ru.endlesscode.mimic.command

import co.aikar.commands.CommandCompletionContext
import co.aikar.commands.CommandCompletions

internal inline fun <reified T : Enum<T>> CommandCompletions<*>.registerEnumCompletion(
    id: String = T::class.java.simpleName.toLowerCase()
): CommandCompletions.CommandCompletionHandler<out CommandCompletionContext<*>>? {
    return registerEnumCompletion(id, enumValues<T>())
}

internal fun <T : Enum<T>> CommandCompletions<*>.registerEnumCompletion(
    id: String,
    values: Array<T>
): CommandCompletions.CommandCompletionHandler<out CommandCompletionContext<*>>? {
    return registerStaticCompletion(id, values.map { it.name.toLowerCase() })
}
