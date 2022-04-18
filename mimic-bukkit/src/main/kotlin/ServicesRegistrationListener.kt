package ru.endlesscode.mimic

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServiceRegisterEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.ServicesManager
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.level.BukkitLevelSystem

internal class ServicesRegistrationListener(
    private val servicesManager: ServicesManager,
    private val mimic: Mimic,
) : Listener {

    @EventHandler
    fun onServiceRegistered(event: ServiceRegisterEvent) {
        val service = event.provider.provider
        if (service is MimicService && service !is WrappedMimicService) {
            val serviceClass = event.provider.service
            val plugin = event.provider.plugin
            Log.w(
                """
                Service ${serviceClass.name} with id '${service.id}' registered in deprecated way.
                Please ask the ${plugin.name} authors (${plugin.description.authors.joinToString()}) to migrate
                to the new service registration API introduced in Mimic v0.7:
                   https://github.com/EndlessCodeGroup/Mimic/releases/tag/v0.7
                """.trimIndent()
            )
            @Suppress("UNCHECKED_CAST") // We have checked before that service is instance of MimicService
            registerService(event.provider as RegisteredServiceProvider<MimicService>)
        }
    }

    private fun registerService(provider: RegisteredServiceProvider<MimicService>) {
        Log.i("Trying to register service using new API...",)
        val service = mimic.registerService(provider.provider, provider.plugin, provider.priority)
        if (service != null) {
            servicesManager.unregister(provider.service, provider.provider)
        } else {
            Log.w("Service registration using the new API has failed.")
        }
    }

    private fun Mimic.registerService(service: MimicService, plugin: Plugin, priority: ServicePriority): MimicService? {
        return when (service) {
            is BukkitClassSystem.Provider -> registerClassSystem(service, MimicApiLevel.VERSION_0_7, plugin, priority)
            is BukkitItemsRegistry -> registerItemsRegistry(service, MimicApiLevel.VERSION_0_7, plugin, priority)
            is BukkitLevelSystem.Provider -> registerLevelSystem(service, MimicApiLevel.VERSION_0_7, plugin, priority)

            else -> {
                Log.w("Unknown service: ${service.javaClass.name}")
                null
            }
        }
    }
}
