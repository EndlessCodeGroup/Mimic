package ru.endlesscode.mimic.blocks

import ru.endlesscode.mimic.ExperimentalMimicApi

/**
 * Consumes items dropped by a block broken via [BlockRegistry.breakBlock].
 * @since 0.9.0
 */
@ExperimentalMimicApi
public fun interface DropsConsumer<ItemStackT : Any> {

    /** Consumes the [drops] and returns the items to drop on the ground near the broken block. */
    public fun consume(drops: Collection<ItemStackT>): Collection<ItemStackT>

    public companion object {
        /** Drops all items on the ground. */
        @JvmStatic
        public fun <ItemStackT : Any> drop(): DropsConsumer<ItemStackT> = DropsConsumer { it }

        /** Drops nothing, the items disappear. */
        @JvmStatic
        public fun <ItemStackT : Any> disappear(): DropsConsumer<ItemStackT> = DropsConsumer { emptyList() }
    }
}
