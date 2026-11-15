// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mimic

import net.kyori.adventure.key.Key
import ru.endlesscode.mimic.MimicService
import ru.endlesscode.mimic.ServiceEntry
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.internal.authorsMention

/**
 * Providers of an API that combines values of several plugins, like Levels or Classes.
 * Each provider owns the namespace equal to its ID. Providers return paths, and Mimic adds the namespace:
 * `mining` from `auraskills` becomes `auraskills:mining`, see [key].
 * Selects providers by namespace or by the [preferred provider ID][preferredProviderId] from the config.
 * Paths returned by providers are checked with [isValidPath] and [validValues].
 *
 * @property primaryName Name of the primary value in messages, like `primary track`.
 */
internal class NamespacedProviders<T : MimicService>(
    private val registry: ServiceRegistry<T>,
    private val primaryName: String,
    private val preferredProviderId: () -> String,
) {

    /** Enabled providers in order of priority, Mimic default implementations last. */
    val enabled: List<ServiceEntry<T>>
        get() = registry.enabledEntries

    /** IDs of all registered providers, including disabled ones. */
    val allIds: Set<String>
        get() = registry.entries.mapTo(LinkedHashSet()) { it.service.id }

    /**
     * The preferred provider, or the provider with the highest priority, shown as selected in the config.
     * It is only an approximation: without a preference, [selectPrimary] skips providers without a primary value.
     * It is `null` only while Mimic isn't enabled, before its `onEnable` and after its `onDisable`,
     * because [enabled] skips providers of disabled plugins, including Mimic's own default provider.
     */
    val primary: ServiceEntry<T>?
        get() = selectPrimary { it }

    /**
     * Returns the primary value of the preferred provider, even if it is `null`.
     * If there is no preferred provider, returns the first primary value of providers in order of priority.
     */
    inline fun <R : Any> selectPrimary(select: (ServiceEntry<T>) -> R?): R? {
        val providers = enabled
        val preferred = preferred(providers)
        if (preferred != null) return select(preferred)
        return providers.firstNotNullOfOrNull(select)
    }

    /** Returns the provider selected in the config, or `null` if it isn't selected or isn't available. */
    fun preferred(providers: List<ServiceEntry<T>>): ServiceEntry<T>? {
        val preferredId = preferredProviderId()
        if (preferredId.isEmpty()) return null
        val provider = providers.find { it.service.id == preferredId }
        if (provider == null) {
            registry.warnings.warn("unknown-preferred-provider:$preferredId") {
                "${registry.apiName} '$preferredId' set in config isn't available, " +
                    "the $primaryName is taken from other providers."
            }
        }
        return provider
    }

    /**
     * Returns the enabled provider owning the namespace of the given [id], or `null` if there is no such provider.
     *
     * Warns once if the [id] has the `minecraft:` namespace and doesn't exist, suggesting existing IDs with the same path.
     * `Key.key(String)` adds the `minecraft:` namespace, so an unknown `minecraft:` ID likely misses its namespace.
     * Only for such IDs, [contains] is called to check whether a provider has a value with the given path.
     * The [valueName] is used in messages, like `track`.
     */
    fun find(id: Key, valueName: String, contains: ServiceEntry<T>.(path: String) -> Boolean): ServiceEntry<T>? {
        val provider = enabled.find { it.service.id == id.namespace() }
        if (id.namespace() == Key.MINECRAFT_NAMESPACE && (provider == null || !provider.contains(id.value()))) {
            registry.warnings.warn("unknown-minecraft-$valueName:${id.asString()}") {
                val suggestions = enabled
                    .filter { it.service.id != Key.MINECRAFT_NAMESPACE && it.contains(id.value()) }
                    .map { it.key(id.value()) }
                buildString {
                    append("${valueName.replaceFirstChar(Char::uppercase)} '${id.asString()}' doesn't exist. ")
                    append("${valueName.replaceFirstChar(Char::uppercase)} ID should have the namespace of its plugin, ")
                    append("but `Key.key(\"${id.value()}\")` adds 'minecraft:'.")
                    if (suggestions.isNotEmpty()) append(" Did you mean ${suggestions.joinToString { "'${it.asString()}'" }}?")
                }
            }
        }
        return provider
    }
}

/** Returns the full ID of the [path] returned by this provider: `mining` -> `auraskills:mining`. */
internal fun ServiceEntry<*>.key(path: String): Key = Key.key(service.id, path)

/**
 * Returns the given [values] of this provider, skipping values with invalid or duplicated IDs.
 * The [valueName] is used in messages, like `track`.
 */
internal fun <V> ServiceEntry<*>.validValues(values: Collection<V>, valueName: String, id: (V) -> String): List<V> =
    values
        .filter { isValidPath(id(it), valueName) }
        .groupBy(id)
        .map { (path, sameValues) ->
            if (sameValues.size > 1) {
                warnings.warn("duplicate-$valueName:${service.id}:$path") {
                    "$apiName '${service.id}' has ${sameValues.size} ${valueName}s with the same ID '$path', " +
                        "only the first one is used. Please, report it to ${plugin.authorsMention}."
                }
            }
            sameValues.first()
        }

/** Returns `true` if the [path] returned by this provider is valid, otherwise logs a warning once. */
internal fun ServiceEntry<*>.isValidPath(path: String, valueName: String): Boolean {
    val isValid = path.isNotEmpty() && Key.parseableValue(path)
    if (!isValid) {
        warnings.warn("invalid-$valueName:${service.id}:$path") {
            "$apiName '${service.id}' returned invalid $valueName ID '$path', such ${valueName}s are ignored. " +
                "${valueName.replaceFirstChar(Char::uppercase)} IDs may contain only a-z, 0-9, '.', '_', '-' and '/'. " +
                "Please, report it to ${plugin.authorsMention}."
        }
    }
    return isValid
}
