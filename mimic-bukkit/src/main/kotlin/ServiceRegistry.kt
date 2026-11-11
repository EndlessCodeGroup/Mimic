package ru.endlesscode.mimic

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority
import ru.endlesscode.mimic.internal.Log

/**
 * Registry of implementations of a single API, keyed by service ID.
 *
 * A service is enabled if its plugin is enabled, the service itself is enabled, and its ID is not in [disabledIds].
 *
 * [defaultId] is the ID of Mimic default implementation.
 * It can be registered only by Mimic itself and is always placed after other services, regardless of priority.
 * ID `mimic` is reserved for Mimic too.
 *
 * Registration rules for a service with an already registered ID:
 * - A service registered again by the same plugin replaces the previous one.
 * - A service registered by another plugin replaces the one built into Mimic, regardless of priority.
 * - A service built into Mimic is skipped if another plugin has already registered the ID.
 * - Otherwise, the service with the higher priority is kept. On equal priorities, the first registered is kept.
 */
internal class ServiceRegistry<T : MimicService>(
    val apiName: String,
    private val mimicPlugin: Plugin,
    private val defaultId: String,
    private val disabledIds: () -> Set<String> = { emptySet() },
) {

    /** Registered services sorted by priority from the highest to the lowest, default implementations last. */
    @Volatile
    var entries: List<ServiceEntry<T>> = emptyList()
        private set

    /** Enabled services in the same order as [entries]. */
    val enabledEntries: List<ServiceEntry<T>>
        get() {
            val disabledIds = disabledIds()
            return entries.filter { (service, plugin) ->
                plugin.isEnabled && service.isEnabled && service.id !in disabledIds
            }
        }

    /** Registers the given [service], returns `true` if it was registered. */
    @Synchronized
    fun register(service: T, plugin: Plugin, priority: ServicePriority): Boolean {
        val id = service.id
        val isBuiltIn = plugin == mimicPlugin
        if (!isBuiltIn && (id == defaultId || id == MIMIC_ID)) {
            Log.w("$apiName '$id' by $plugin is not registered, because the ID is reserved by Mimic.")
            return false
        }

        val existing = entries.find { it.service.id == id }
        if (existing != null) {
            val existingIsBuiltIn = existing.plugin == mimicPlugin
            when {
                existing.plugin == plugin -> Log.i("$apiName '$id' re-registered by $plugin")

                existingIsBuiltIn && !isBuiltIn -> Log.i("Built-in $apiName '$id' replaced by $plugin")

                !existingIsBuiltIn && isBuiltIn -> {
                    Log.i("Built-in $apiName '$id' skipped, because it is already registered by ${existing.plugin}")
                    return false
                }

                priority > existing.priority -> Log.w(
                    "$apiName '$id' by ${existing.plugin} is replaced by $plugin with higher priority ($priority)."
                )

                else -> {
                    Log.w(
                        "$apiName '$id' by $plugin is not registered, " +
                            "because ${existing.plugin} has already registered it with priority ${existing.priority}."
                    )
                    return false
                }
            }
        }

        // Sorting is stable, so services with equal priority keep registration order.
        // A replacing service takes the place of the replaced one.
        val entry = ServiceEntry(service, plugin, priority)
        val newEntries = if (existing == null) entries + entry else entries.map { if (it === existing) entry else it }
        entries = newEntries.sortedWith(
            compareBy<ServiceEntry<T>> { it.service.id == defaultId }.thenByDescending { it.priority }
        )
        return true
    }

    /** Unregisters all services registered by the given [plugin]. */
    @Synchronized
    fun unregisterAll(plugin: Plugin) {
        entries = entries.filter { it.plugin != plugin }
    }

    private companion object {
        /** Namespace of Mimic's combining registries. */
        const val MIMIC_ID = "mimic"
    }
}

internal data class ServiceEntry<T : MimicService>(
    val service: T,
    val plugin: Plugin,
    val priority: ServicePriority,
)
