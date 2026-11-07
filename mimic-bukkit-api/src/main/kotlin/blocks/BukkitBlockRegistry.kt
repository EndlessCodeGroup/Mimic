package ru.endlesscode.mimic.blocks

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.ExperimentalMimicApi

/**
 * [BlockRegistry] for Bukkit.
 * @since 0.9.0
 */
@ExperimentalMimicApi
public interface BukkitBlockRegistry : BlockRegistry<Block, ItemStack>
