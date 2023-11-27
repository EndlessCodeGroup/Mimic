package ru.endlesscode.mimic.config

import org.bukkit.configuration.Configuration
import org.bukkit.configuration.ConfigurationSection

internal class StringSetConfigProperty(
    override val path: String,
    override val title: String,
    override val comments: Array<String> = emptyArray(),
) : ConfigProperty<Set<String>> {
    override val defaultValue: List<String> = emptyList()
    override fun ConfigurationSection.get(): Set<String> = getStringList(path).map { it.lowercase() }.toSet()
    override fun ConfigurationSection.set(value: Set<String>) {
        set(path, value.toList())
    }
}

internal class StringConfigProperty(
    override val path: String,
    override val title: String,
    override val comments: Array<String> = emptyArray(),
) : ConfigProperty<String> {
    override val defaultValue: String = ""
    override fun ConfigurationSection.get(): String = getString(path).orEmpty().lowercase()
    override fun ConfigurationSection.set(value: String) {
        set(path, value)
    }
}

internal sealed interface ConfigProperty<T : Any> {
    val path: String
    val title: String
    val comments: Array<String>
    val defaultValue: Any

    val formattedTitle: String get() = "[$title]"

    fun ConfigurationSection.get(): T
    fun ConfigurationSection.set(value: T)
}

internal fun Configuration.addDefault(property: ConfigProperty<*>) {
    addDefault(property.path, property.defaultValue)
}

internal operator fun <T : Any> ConfigurationSection.get(property: ConfigProperty<T>): T {
    return with(property) { get() }
}

internal operator fun <T : Any> ConfigurationSection.set(property: ConfigProperty<T>, value: T) {
    with(property) { set(value) }
}
