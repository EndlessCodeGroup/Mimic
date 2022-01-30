package ru.endlesscode.mimic.level

import org.bukkit.entity.Player
import java.util.function.Function

internal class DefaultLevelSystemProvider(
    private val provider: Function<Player, out BukkitLevelSystem>,
) : BukkitLevelSystem.Provider {
    override fun getSystem(player: Player): BukkitLevelSystem = provider.apply(player)
}
