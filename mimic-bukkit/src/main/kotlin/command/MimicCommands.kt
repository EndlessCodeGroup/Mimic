package ru.endlesscode.mimic.command

import dev.jorel.commandapi.CommandAPI
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.plugin.java.JavaPlugin
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.internal.Log

internal class MimicCommands {

    private var registered = false
    private var audiences: BukkitAudiences? = null

    fun register(
        plugin: JavaPlugin,
        mimic: Mimic,
        config: MimicConfig,
    ) {
        val commandApiPlugin = plugin.server.pluginManager.getPlugin("CommandAPI")
        if (commandApiPlugin == null) {
            Log.w("CommandAPI not found. Mimic commands won't be registered.")
            Log.w("Consider installing CommandAPI: https://docs.commandapi.dev/")
            return
        } else if (!commandApiPlugin.isEnabled) {
            Log.w("CommandAPI loaded, but not enabled. Mimic commands won't be registered.")
            return
        }

        registered = true
        audiences = BukkitAudiences.create(plugin)

        registerCommand(
            mimic = mimic,
            config = config,
            pluginFullName = plugin.description.fullName,
            audiences = checkNotNull(audiences),
        )
    }

    fun unregister() {
        if (registered) {
            CommandAPI.unregister("mimic")
            audiences?.close()
            audiences = null
        }
    }

}