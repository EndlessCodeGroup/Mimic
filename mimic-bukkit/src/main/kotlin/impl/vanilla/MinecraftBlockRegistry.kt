package ru.endlesscode.mimic.impl.vanilla

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Campfire
import org.bukkit.block.Chest
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.BlockInventoryHolder
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.blocks.BukkitBlockRegistry
import ru.endlesscode.mimic.blocks.DropsConsumer
import ru.endlesscode.mimic.internal.Log

/**
 * Block registry implementation using material name as blockId.
 * Supports block state properties in the vanilla format as a payload in [setBlock], for example `[facing=north]`.
 *
 * Contents of blocks holding items, like chests or campfires, are passed to the drops consumer in [breakBlock].
 * Shulker boxes keep their contents inside the dropped item.
 */
@ExperimentalMimicApi
public class MinecraftBlockRegistry : BukkitBlockRegistry {

    override val id: String = ID

    override val knownIds: List<String> by lazy {
        blockMaterials.map { it.name.lowercase() }
    }

    /** Maps items to blocks placed by these items, for example `wheat_seeds` -> `wheat`. */
    private val blocksByItem: Map<Material, Material> by lazy {
        blockMaterials
            .groupBy { it.createBlockData().placementMaterial }
            .filterKeys { !it.isAir }
            .mapValues { (item, blocks) -> blocks.firstOrNull { it == item } ?: blocks.first() }
    }

    private val blockMaterials: List<Material>
        get() = Material.entries.filter { it.isBlock && !it.isLegacy }

    override fun isBlockExists(blockId: String): Boolean = getMaterial(blockId) != null

    override fun getBlockId(block: Block): String = block.type.name.lowercase()

    override fun getBlockIdFromItem(blockItem: ItemStack): String? {
        if (blockItem.isEmpty) return null
        return blocksByItem[blockItem.type]?.name?.lowercase()
    }

    override fun setBlock(block: Block, blockId: String, payload: Any?): Boolean {
        if (!block.isEmpty) return false
        val material = getMaterial(blockId) ?: return false

        val blockData = when (payload) {
            is String -> try {
                Bukkit.createBlockData(material, payload)
            } catch (e: IllegalArgumentException) {
                Log.w(e, "[$TAG] Ignoring invalid payload for block $blockId: $payload")
                null
            }

            else -> {
                if (payload != null) Log.w("[$TAG] Ignoring unsupported payload for block $blockId:\n$payload")
                null
            }
        }

        if (blockData != null) block.blockData = blockData else block.type = material
        return true
    }

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer<ItemStack>): Boolean {
        if (block.isEmpty) return false

        val drops = block.getDrops(tool) + takeContents(block)
        block.type = Material.AIR

        for (item in dropsConsumer.consume(drops)) {
            block.world.dropItemNaturally(block.location, item)
        }
        return true
    }

    /** Takes items held by the [block], so they will not be dropped when the block is removed. */
    private fun takeContents(block: Block): List<ItemStack> = when (val state = block.getState(false)) {
        is ShulkerBox -> emptyList()
        is BlockInventoryHolder -> {
            // Take only this half of a double chest
            val inventory = if (state is Chest) state.blockInventory else state.inventory
            val contents = inventory.contents.filterNotNull().filterNot { it.isEmpty }
            inventory.clear()
            contents
        }

        is Campfire -> (0 until state.size).mapNotNull { slot ->
            state.getItem(slot)
                ?.takeUnless { it.isEmpty }
                .also { state.setItem(slot, null) }
        }

        else -> emptyList()
    }

    private fun getMaterial(name: String): Material? =
        Material.getMaterial(name.uppercase())?.takeIf { it.isBlock && !it.isLegacy }

    public companion object {
        public const val ID: String = "minecraft"
        private const val TAG = "MinecraftBlockRegistry"
    }
}
