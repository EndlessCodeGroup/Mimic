package ru.endlesscode.mimic.classes

import org.bukkit.entity.Player
import java.util.function.Function

internal class DefaultClassSystemProvider(
    private val provider: Function<Player, out BukkitClassSystem>,
) : BukkitClassSystem.Provider {
    override fun getSystem(player: Player): BukkitClassSystem = provider.apply(player)
}
