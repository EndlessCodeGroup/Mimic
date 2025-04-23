package ru.endlesscode.mimic.blocks

import blocks.BlocksRegistry
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.ExperimentalMimicApi

/**
 * [BlocksRegistry] for Bukkit.
 * @since 0.9.0
 */
@ExperimentalMimicApi
public interface BukkitBlocksRegistry : BlocksRegistry<ItemStack, Block>
