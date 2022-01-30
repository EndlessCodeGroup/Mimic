package ru.endlesscode.mimic.level

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.lang.reflect.Constructor

internal class DefaultLevelSystemProvider(
    plugin: Plugin,
    private val constructor: Constructor<out BukkitLevelSystem>,
) : BukkitLevelSystem.Provider(plugin.name) {

    private val pluginManager = plugin.server.pluginManager

    override val isEnabled: Boolean
        get() = pluginManager.isPluginEnabled(id)

    override fun getSystem(player: Player): BukkitLevelSystem = constructor.newInstance(player)
}
