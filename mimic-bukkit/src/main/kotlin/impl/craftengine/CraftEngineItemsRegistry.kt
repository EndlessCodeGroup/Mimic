package ru.endlesscode.mimic.impl.craftengine

import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.items.BukkitItemsRegistry

/**
 * Items registry for CraftEngine custom items.
 * CraftEngine namespace becomes the first path segment of item ID: `default:palm_log` -> `default/palm_log`.
 * Vanilla items modified by CraftEngine are left to the vanilla registry.
 */
public class CraftEngineItemsRegistry private constructor(
    private val craftEngine: CraftEngineWrapper,
) : BukkitItemsRegistry {

    internal constructor() : this(CraftEngineWrapper())

    override val id: String = ID
    override val isEnabled: Boolean get() = craftEngine.isEnabled

    override val knownIds: Collection<String>
        get() = craftEngine.loadedCustomItems.map { it.id().toMimicId() }

    override fun isItemExists(itemId: String): Boolean = findItem(itemId) != null

    override fun getItemId(item: ItemStack): String? = craftEngine.getCustomItem(item)?.id()?.toMimicId()

    override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? {
        val key = craftEngineKey(itemId) ?: return null
        return craftEngine.createCustomItem(key, amount)
    }

    private fun findItem(itemId: String): BukkitItemDefinition? {
        val key = craftEngineKey(itemId) ?: return null
        return craftEngine.getCustomItem(key)
    }

    public companion object {
        public const val ID: String = "craftengine"
    }
}
