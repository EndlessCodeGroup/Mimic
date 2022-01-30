package ru.endlesscode.mimic.classes

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.function.Function

internal class DefaultClassSystemProvider(
    plugin: Plugin,
    private val provider: Function<Player, out BukkitClassSystem>,
) : BukkitClassSystem.Provider(plugin.name) {

    private val pluginManager = plugin.server.pluginManager

    override val isEnabled: Boolean
        get() = pluginManager.isPluginEnabled(id)

    override fun getSystem(player: Player): BukkitClassSystem = provider.apply(player)
}
