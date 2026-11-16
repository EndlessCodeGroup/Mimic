// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.itemsadder

import dev.lone.itemsadder.api.CustomBlock
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.blocks.DropsConsumer
import kotlin.test.Test

internal class ItemsAdderBlockProviderTest : BukkitTestBase() {

    private val world: World = mockk(relaxed = true)
    private val location = Location(world, 1.0, 2.0, 3.0)
    private val block: Block = mockk {
        every { this@mockk.world } returns this@ItemsAdderBlockProviderTest.world
        every { this@mockk.location } returns this@ItemsAdderBlockProviderTest.location
    }

    private val itemsAdder: ItemsAdderWrapper = mockk {
        every { blockIds } returns setOf("itemsadder:ruby_ore", "my_blocks:ruby_ore")
        every { hasBlock(any()) } answers { firstArg<String>() in blockIds }
        every { getCustomBlock(any<String>()) } returns null
        every { getCustomBlock("my_blocks:ruby_ore") } answers { mockCustomBlock() }
        every { getCustomBlock(any<ItemStack>()) } returns null
        every { getCustomBlock(any<Block>()) } returns null
        every { placeBlock(any(), any()) } answers { firstArg<String>() in blockIds }
    }

    // SUT
    private val provider = ItemsAdderBlockProvider(itemsAdder)

    @Test
    fun `when get all blocks - should drop default namespace and keep others as the first segment`() {
        provider.allBlocks shouldContainExactly listOf("ruby_ore", "my_blocks/ruby_ore")
    }

    @ParameterizedTest
    @CsvSource(
        "ruby_ore,           true",
        "my_blocks/ruby_ore, true",
        "my_blocks/unknown,  false",
        "unknown,            false",
    )
    fun `when check block exists - should look for ItemsAdder id`(blockId: String, expected: Boolean) {
        provider.hasBlock(blockId) shouldBe expected
    }

    @Test
    fun `when get block id - should return id with namespace as the first segment`() {
        every { itemsAdder.getCustomBlock(block) } returns mockCustomBlock()

        provider.getBlockId(block) shouldBe "my_blocks/ruby_ore"
    }

    @Test
    fun `when get id of not ItemsAdder block - should return null`() {
        provider.getBlockId(block).shouldBeNull()
    }

    @Test
    fun `when get block id from item - should return id of the block`() {
        val item = mockk<ItemStack>()
        every { itemsAdder.getCustomBlock(item) } returns mockCustomBlock()

        provider.getBlockIdFromItem(item) shouldBe "my_blocks/ruby_ore"
    }

    @Test
    fun `when get block id from item not placing a block - should return null`() {
        provider.getBlockIdFromItem(ItemStack(Material.PAPER)).shouldBeNull()
    }

    @Test
    fun `when create block item - should return the item with the given amount`() {
        val item = provider.createBlockItem("my_blocks/ruby_ore", amount = 3)

        item?.type shouldBe Material.PAPER
        item?.amount shouldBe 3
    }

    @ParameterizedTest
    @ValueSource(strings = ["unknown", "my_blocks/unknown"])
    fun `when create item of unknown block - should return null`(blockId: String) {
        provider.createBlockItem(blockId, amount = 1).shouldBeNull()
    }

    @Test
    fun `when set block - should place the block by ItemsAdder id`() {
        provider.setBlock(block, "my_blocks/ruby_ore", state = null) shouldBe true

        verify { itemsAdder.placeBlock("my_blocks:ruby_ore", location) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["my_blocks/ruby_ore", "my_blocks/unknown"])
    fun `when set block with state - should not place the block`(blockId: String) {
        provider.setBlock(block, blockId, state = "[facing=north]") shouldBe false

        verify(exactly = 0) { itemsAdder.placeBlock(any(), any()) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["unknown", "my_blocks/unknown"])
    fun `when set unknown block - should return false`(blockId: String) {
        provider.setBlock(block, blockId, state = null) shouldBe false
    }

    @Test
    fun `when break block - should remove the block and drop items returned by the consumer`() {
        val tool = mockk<ItemStack>()
        val loot = listOf<ItemStack>(mockk(), mockk())
        val customBlock = mockCustomBlock()
        every { customBlock.getLoot(tool, true) } returns loot
        every { itemsAdder.getCustomBlock(block) } returns customBlock
        var consumed: Collection<ItemStack>? = null
        val dropsConsumer = DropsConsumer { drops ->
            consumed = drops
            drops.take(1)
        }

        provider.breakBlock(block, tool, dropsConsumer) shouldBe true

        consumed shouldBe loot
        verify { customBlock.remove() }
        verify(exactly = 1) { world.dropItemNaturally(location, any()) }
        verify { world.dropItemNaturally(location, loot.first()) }
    }

    @Test
    fun `when break not ItemsAdder block - should return false`() {
        provider.breakBlock(block, tool = null, DropsConsumer.drop()) shouldBe false
    }

    @Test
    fun `when break block that can't be removed - should drop nothing`() {
        val customBlock = mockCustomBlock()
        every { customBlock.remove() } returns false
        every { itemsAdder.getCustomBlock(block) } returns customBlock

        provider.breakBlock(block, tool = null, DropsConsumer.drop()) shouldBe false

        verify(exactly = 0) { world.dropItemNaturally(any(), any()) }
    }

    private fun mockCustomBlock(): CustomBlock = mockk {
        every { namespacedID } returns "my_blocks:ruby_ore"
        every { itemStack } returns ItemStack(Material.PAPER)
        every { getLoot(any(), any()) } returns listOf(ItemStack(Material.DIAMOND))
        every { remove() } returns true
    }
}
