// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mimic

import net.kyori.adventure.key.Key
import ru.endlesscode.mimic.MimicService
import ru.endlesscode.mimic.ServiceEntry
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.internal.authorsMention
import ru.endlesscode.mimic.logImplementationError

/**
 * Providers of an API that combines values of several plugins, like Levels or Blocks.
 * Each provider owns the namespace equal to its ID. Providers return paths, and Mimic adds the namespace:
 * `mining` from `auraskills` becomes `auraskills:mining`, see [key].
 * Selects providers by namespace, or by the value they recognize, see [owner].
 * Paths returned by providers are checked with [isValidPath] and [validValues].
 *
 * @property hasVanillaValues `true` if the `minecraft:` namespace holds many vanilla values, like blocks.
 *   Then an unknown `minecraft:` ID is rather a typo than a missing namespace, see [find].
 */
internal open class NamespacedProviders<T : MimicService>(
    val registry: ServiceRegistry<T>,
    private val hasVanillaValues: Boolean = false,
) {

    /** Enabled providers in order of priority, Mimic default implementations last. */
    val enabled: List<ServiceEntry<T>>
        get() = registry.enabledEntries

    /** IDs of all registered providers, including disabled ones. */
    val allIds: Set<String>
        get() = registry.entries.mapTo(LinkedHashSet()) { it.service.id }

    /**
     * Returns the provider with the highest priority recognizing a value paired with the full ID of the value,
     * or `null` if no provider recognizes it. The [getPath] returns the ID of the value in the given provider.
     * The [valueName] is used in messages, like `block`.
     *
     * If a provider fails to check the value or returns an invalid ID, the default provider is skipped,
     * because it recognizes any value, including vanilla values backing custom ones.
     */
    inline fun owner(valueName: String, getPath: T.() -> String?): Pair<ServiceEntry<T>, Key>? {
        var failed = false
        for (provider in enabled) {
            if (failed && provider.service.id == registry.defaultId) continue
            val path = runCatching { provider.service.getPath() }
                .onFailure { cause ->
                    provider.logImplementationError(cause)
                    failed = true
                }
                .getOrNull() ?: continue

            if (!provider.isValidPath(path, valueName)) {
                failed = true
                continue
            }
            return provider to provider.key(path)
        }
        return null
    }

    /**
     * Returns the enabled provider owning the namespace of the given [id], or `null` if there is no such provider.
     *
     * Warns once if the [id] has the `minecraft:` namespace and doesn't exist, suggesting existing IDs with the same path.
     * `Key.key(String)` adds the `minecraft:` namespace, so an unknown `minecraft:` ID likely misses its namespace.
     * If the API [has vanilla values][hasVanillaValues], warns only when there are IDs to suggest.
     * Only for such IDs, [contains] is called to check whether a provider has a value with the given path.
     * The [valueName] is used in messages, like `track`.
     */
    fun find(id: Key, valueName: String, contains: ServiceEntry<T>.(path: String) -> Boolean): ServiceEntry<T>? {
        val provider = enabled.find { it.service.id == id.namespace() }
        if (id.namespace() == Key.MINECRAFT_NAMESPACE && (provider == null || !provider.contains(id.value()))) {
            val suggestions by lazy {
                enabled
                    .filter { it.service.id != Key.MINECRAFT_NAMESPACE && it.contains(id.value()) }
                    .map { it.key(id.value()) }
            }
            if (hasVanillaValues && suggestions.isEmpty()) return provider
            registry.warnings.warn("unknown-minecraft-$valueName:${id.asString()}") {
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

/**
 * [NamespacedProviders] of an API that has a primary value, like Levels or Classes.
 * Selects the provider of the primary value by the [preferred provider ID][preferredProviderId] from the config.
 *
 * @property primaryName Name of the primary value in messages, like `primary track`.
 */
internal class PrimaryNamespacedProviders<T : MimicService>(
    registry: ServiceRegistry<T>,
    private val primaryName: String,
    private val preferredProviderId: () -> String,
) : NamespacedProviders<T>(registry) {

    /**
     * The preferred provider, or the provider with the highest priority, shown as selected in the config.
     * It is only an approximation: without a preference, [selectPrimary] skips providers without a primary value.
     * It is `null` if there are no enabled providers: the API has no default provider and no plugin provides it,
     * or Mimic isn't enabled, because [enabled] skips providers of disabled plugins.
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
