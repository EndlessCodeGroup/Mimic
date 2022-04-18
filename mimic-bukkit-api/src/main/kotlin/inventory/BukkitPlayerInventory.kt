package ru.endlesscode.mimic.inventory

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.PlayerSystemProviderService
import ru.endlesscode.mimic.internal.filterNotNullOrEmpty
import ru.endlesscode.mimic.util.ExistingWeakReference

/** [PlayerInventory] for Bukkit. */
@ExperimentalMimicApi
@ApiStatus.Experimental
public abstract class BukkitPlayerInventory(player: Player) : PlayerInventory<ItemStack> {

    /** The player owning this inventory. */
    public val player: Player get() = playerRef.get()

    private val playerRef = ExistingWeakReference(player)

    /**
     * Helper method to get player's inventory contents that is not `null` or AIR.
     *
     * Appends the given [additionalItems] to this list. Be careful, to avoid items duplicates.
     * Elements in the returned list doesn't contain item held in the main hand.
     */
    @JvmOverloads
    protected fun collectStoredItems(additionalItems: List<ItemStack?> = emptyList()): List<ItemStack> {
        val playerInventory = player.inventory
        return playerInventory.storageContents
            .asSequence()
            .plus(additionalItems)
            .minus(playerInventory.itemInMainHand)
            .filterNotNullOrEmpty()
            .toList()
    }

    /**
     * Helper method to get list of player's equipped items. Includes armor and items in hands.
     * Doesn't contain `null` or AIR.
     *
     * Appends the given [additionalItems] to this list. Be careful, to avoid items duplicates.
     */
    @JvmOverloads
    public fun collectEquippedItems(additionalItems: List<ItemStack?> = emptyList()): List<ItemStack> {
        val playerInventory = player.inventory
        return sequenceOf(playerInventory.itemInMainHand, playerInventory.itemInOffHand)
            .plus(playerInventory.armorContents)
            .plus(additionalItems)
            .filterNotNullOrEmpty()
            .toList()
    }

    /** Provider for [BukkitPlayerInventory]. */
    public fun interface Provider : PlayerSystemProviderService<BukkitPlayerInventory>
}
