// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.blocks

import org.bukkit.inventory.ItemStack

/**
 * Consumes items dropped by a block broken via [Blocks.breakBlock].
 * @since 0.11.0
 */
public fun interface DropsConsumer {

    /**
     * Consumes the [drops] and returns the items to drop on the ground near the broken block.
     * It should not throw exceptions, because the block may already be removed when it is called.
     */
    public fun consume(drops: Collection<ItemStack>): Collection<ItemStack>

    public companion object {
        /** Drops all items on the ground. */
        @JvmStatic
        public fun drop(): DropsConsumer = DropsConsumer { it }

        /** Drops nothing, the items disappear. */
        @JvmStatic
        public fun disappear(): DropsConsumer = DropsConsumer { emptyList() }
    }
}
