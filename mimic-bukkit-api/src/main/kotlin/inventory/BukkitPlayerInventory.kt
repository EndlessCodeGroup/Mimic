package ru.endlesscode.mimic.inventory

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.PlayerSystemProviderService
import ru.endlesscode.mimic.util.ExistingWeakReference

/** [PlayerInventory] for Bukkit. */
@ExperimentalMimicApi
public abstract class BukkitPlayerInventory(player: Player) : PlayerInventory<ItemStack> {

    /** The player owning this inventory. */
    public val player: Player get() = playerRef.get()

    private val playerRef = ExistingWeakReference(player)

    /** Provider for [BukkitPlayerInventory]. */
    public fun interface Provider : PlayerSystemProviderService<BukkitPlayerInventory>
}
