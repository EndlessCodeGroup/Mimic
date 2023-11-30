package blocks

import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.MimicService

/**
 * Registry to get and place blocks.
 * @since 0.9.0
 */
@ExperimentalMimicApi
public interface BlocksRegistry<ItemStackT : Any, BlockT : Any> : MimicService {

    /** Returns all known block IDs. */
    public val knownIds: Collection<String>

    /** Returns `true` if given [block] represented with given [blockId]. */
    public fun isSameBlock(block: BlockT, blockId: String): Boolean = getBlockId(block) == blockId

    /** Returns `true` if given [blockItem] is an item stack containing the block represented with given [blockId]. */
    public fun isSameBlockItem(blockItem: ItemStackT, blockId: String): Boolean = getBlockItemId(blockItem) == blockId

    /** Returns `true` if block with given [blockId] exists. */
    public fun isBlockExists(blockId: String): Boolean

    /** Returns ID representing given [block], or `null` if the ID not found in this registry. */
    public fun getBlockId(block: BlockT): String?

    /**
     * Returns ID representing the block contained in given [blockItem],
     * or `null` if the ID not found in this registry.
     */
    public fun getBlockItemId(blockItem: ItemStackT): String?

    /** Returns item containing block by given [blockId], or `null` if the ID not found in this registry. */
    public fun getBlockItem(blockId: String): ItemStackT? = getBlockItem(blockId, payload = null, amount = 1)

    /**
     * Returns item containing block with specified [payload] by given [blockId],
     * or `null` if the ID not found in this registry.
     *
     * If [payload] is not `null`, block will be configured using it.
     */
    public fun getBlockItem(blockId: String, payload: Any?): ItemStackT? = getBlockItem(blockId, payload, amount = 1)

    /**
     * Returns item containing block with specified [amount] by given [blockId],
     * or `null` if ID not found in this registry.
     *
     * If the given [amount] is greater than maximum possible, will use maximum possible amount.
     * Amount shouldn't be less than `1`.
     */
    public fun getBlockItem(blockId: String, amount: Int): ItemStackT? = getBlockItem(blockId, payload = null, amount)

    /**
     * Returns item containing block with specified [amount] and [payload] by given [blockId],
     * or `null` if ID not found in this registry.
     *
     * If the given [amount] is greater than maximum possible, will use maximum possible amount.
     * Amount shouldn't be less than `1`.
     *
     * Given [payload] may be used to configure block.
     */
    public fun getBlockItem(blockId: String, payload: Any?, amount: Int): ItemStackT?

    /**
     * Places block by given [blockId] at [destination].
     *
     * Returns `true` if the block was placed, and `false` if ID not found in this registry.
     */
    public fun placeBlock(blockId: String, destination: BlockT): Boolean =
        placeBlock(blockId, payload = null, destination)

    /**
     * Places block with specified [payload] by given [blockId] at [destination].
     *
     * Given [payload] may be used to configure block.
     *
     * Returns `true` if the block was placed, and `false` if ID not found in this registry.
     */
    public fun placeBlock(blockId: String, payload: Any?, destination: BlockT): Boolean
}