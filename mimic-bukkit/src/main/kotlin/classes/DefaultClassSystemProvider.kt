package ru.endlesscode.mimic.classes

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.lang.reflect.Constructor

internal class DefaultClassSystemProvider(
    plugin: Plugin,
    private val constructor: Constructor<out BukkitClassSystem>,
) : BukkitClassSystem.Provider(plugin.name) {

    private val pluginManager = plugin.server.pluginManager

    override val isEnabled: Boolean
        get() = pluginManager.isPluginEnabled(id)

    override fun getSystem(player: Player): BukkitClassSystem = constructor.newInstance(player)
}
