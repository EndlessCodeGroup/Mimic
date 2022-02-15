package ru.endlesscode.mimic.internal

private val notSupportedCalls = mutableSetOf<String>()

internal inline fun <T> callCompat(
    key: String,
    block: () -> T,
    compat: () -> T,
): T {
    if (key !in notSupportedCalls) {
        try {
            return block()
        } catch (_: NoSuchMethodError) {
            notSupportedCalls.add(key)
        }
    }

    return compat()
}
