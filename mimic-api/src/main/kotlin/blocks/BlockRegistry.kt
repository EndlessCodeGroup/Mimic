package ru.endlesscode.mimic.blocks

import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.MimicService

/**
 * Registry to get, set and break blocks.
 *
 * Block IDs are [resource locations](https://minecraft.wiki/w/Resource_location),
 * where the namespace is the ID of a block registry, for example `craftengine:default/palm_log`.
 * IDs without namespace or with the `mimic` namespace are searched over all registries.
 *
 * Implementations work with IDs without the registry namespace.
 * A plugin's own namespace becomes the first path segment: `default:palm_log` -> `default/palm_log`.
 *
 * Methods changing blocks should be called from the server thread.
 * They fire no place or break events and make no protection checks, the caller is responsible for both.
 * Block physics is applied as usual, so neighboring blocks are updated.
 * @since 0.9.0
 */
@ExperimentalMimicApi
public interface BlockRegistry<BlockT : Any, ItemStackT : Any> : MimicService {

    /** Returns currently known block IDs. They may change while plugins load or reload their data. */
    public val knownIds: Collection<String>

    /** Returns `true` if the given [block] has the given [blockId]. */
    public fun isSameBlock(block: BlockT, blockId: String): Boolean = getBlockId(block) == blockId

    /** Returns `true` if the given [blockItem] places the block with the given [blockId]. */
    public fun isSameBlockItem(blockItem: ItemStackT, blockId: String): Boolean =
        getBlockIdFromItem(blockItem) == blockId

    /** Returns `true` if a block with the given [blockId] exists. */
    public fun isBlockExists(blockId: String): Boolean

    /** Returns ID of the given [block], or `null` if the block doesn't belong to this registry. */
    public fun getBlockId(block: BlockT): String?

    /** Returns ID of the block placed by the given [blockItem], or `null` if it doesn't belong to this registry. */
    public fun getBlockIdFromItem(blockItem: ItemStackT): String?

    /**
     * Returns item placing the block with the given [blockId], or `null` if the block has no item or the ID is unknown.
     */
    public fun getBlockItem(blockId: String): ItemStackT? = getBlockItem(blockId, amount = 1)

    /**
     * Returns item stack with the given [amount] placing the block with the given [blockId],
     * or `null` if the block has no item or the ID is unknown.
     * Some blocks, like fire, can't be placed by an item.
     *
     * If given [amount] is greater than maximum possible, will use maximum possible amount.
     * Amount shouldn't be less than `1`.
     */
    public fun getBlockItem(blockId: String, amount: Int): ItemStackT?

    /** Sets the block with the given [blockId] in place of the empty [block]. */
    public fun setBlock(block: BlockT, blockId: String): Boolean = setBlock(block, blockId, payload = null)

    /**
     * Sets the block with the given [blockId] configured with the [payload] in place of the empty [block].
     * To replace a block, break it first, so its registry can clean up.
     *
     * Every implementation supports a [String] payload with block state properties, for example `[facing=north]`.
     * Unsupported payload is ignored.
     *
     * Returns `false` if the [block] is not empty, the ID is unknown, or the block can't be set.
     */
    public fun setBlock(block: BlockT, blockId: String, payload: Any?): Boolean

    /** Breaks the given [block] with a suitable tool, dropping items on the ground. */
    public fun breakBlock(block: BlockT): Boolean = breakBlock(block, tool = null, DropsConsumer.drop())

    /** Breaks the given [block] with the [tool], dropping items on the ground. */
    public fun breakBlock(block: BlockT, tool: ItemStackT?): Boolean =
        breakBlock(block, tool, DropsConsumer.drop())

    /**
     * Breaks the given [block] with the [tool] and passes the dropped items to the [dropsConsumer].
     *
     * If [tool] is `null`, drops are the same as for a suitable tool without enchantments.
     * Pass an empty item to break the block by hand.
     * Only items are dropped, without experience and break effects.
     * Use [DropsConsumer.disappear] to drop nothing.
     *
     * Returns `false` if the block can't be broken or doesn't belong to this registry.
     */
    public fun breakBlock(block: BlockT, tool: ItemStackT? = null, dropsConsumer: DropsConsumer<ItemStackT>): Boolean
}
