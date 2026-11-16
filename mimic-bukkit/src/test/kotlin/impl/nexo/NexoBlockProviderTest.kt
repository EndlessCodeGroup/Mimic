// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.nexo

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.blocks.DropsConsumer
import kotlin.test.Test

internal class NexoBlockProviderTest : BukkitTestBase() {

    private val blockWorld: World = mockk {
        every { dropItemNaturally(any(), any()) } returns mockk()
    }
    private val blockLocation: Location = mockk()
    private val block: Block = mockk {
        every { world } returns blockWorld
        every { location } returns blockLocation
    }
    private val tool: ItemStack = mockk()
    private val ruby: ItemStack = mockk()

    private val nexo: NexoWrapper = mockk(relaxUnitFun = true) {
        every { blockIds } returns listOf("ruby_ore")
        every { hasBlock(any()) } answers { firstArg<String>() in blockIds }
        every { getItemId(any()) } returns null
        every { getBlockId(any()) } returns null
        every { getDrops(any(), any()) } returns null
        every { createItem("ruby_ore", any()) } answers { ItemStack(Material.PAPER, secondArg()) }
    }

    // SUT
    private val provider = NexoBlockProvider(nexo)

    @Test
    fun `when get all blocks - should return Nexo block ids`() {
        provider.allBlocks shouldContainExactly listOf("ruby_ore")
    }

    @Test
    fun `when get block id from block item - should return item id`() {
        every { nexo.getItemId(any()) } returns "ruby_ore"

        provider.getBlockIdFromItem(ItemStack(Material.PAPER)) shouldBe "ruby_ore"
    }

    @Test
    fun `when get block id from not block item - should return null`() {
        every { nexo.getItemId(any()) } returns "ruby"

        provider.getBlockIdFromItem(ruby).shouldBeNull()
    }

    @Test
    fun `when get id of directional block - should return id reported by Nexo wrapper`() {
        every { nexo.getBlockId(block) } returns "ruby_log"

        provider.getBlockId(block) shouldBe "ruby_log"
    }

    @Test
    fun `when Nexo ids are not valid - should convert them`() {
        every { nexo.blockIds } returns listOf("Ruby Ore")
        every { nexo.getItemId(any()) } returns "Ruby Ore"
        every { nexo.getBlockId(block) } returns "Ruby Ore"
        every { nexo.createItem("Ruby Ore", any()) } answers { ItemStack(Material.PAPER) }

        provider.allBlocks shouldContainExactly listOf("ruby_ore")
        provider.hasBlock("ruby_ore").shouldBeTrue()
        provider.getBlockId(block) shouldBe "ruby_ore"
        provider.getBlockIdFromItem(ruby) shouldBe "ruby_ore"
        provider.createBlockItem("ruby_ore", amount = 1)?.type shouldBe Material.PAPER
        provider.setBlock(block, "ruby_ore", state = null).shouldBeTrue()
        verify { nexo.placeBlock(block, "Ruby Ore") }
    }

    @Test
    fun `when create block item - should create Nexo item`() {
        val item = provider.createBlockItem("ruby_ore", amount = 3)

        item?.type shouldBe Material.PAPER
        item?.amount shouldBe 3
    }

    @Test
    fun `when create block item of not block - should return null`() {
        provider.createBlockItem("ruby", amount = 1).shouldBeNull()
        verify(exactly = 0) { nexo.createItem(any(), any()) }
    }

    @Test
    fun `when set block - should place Nexo block`() {
        every { nexo.placeBlock(block, "ruby_ore") } answers {
            every { nexo.getBlockId(block) } returns "ruby_ore"
        }

        provider.setBlock(block, "ruby_ore", state = null).shouldBeTrue()
    }

    @Test
    fun `when set block and Nexo doesn't place it - should return false`() {
        provider.setBlock(block, "ruby_ore", state = null).shouldBeFalse()
    }

    @Test
    fun `when set block with state or unknown block - should not place block`() {
        provider.setBlock(block, "ruby_ore", state = "[facing=north]").shouldBeFalse()
        provider.setBlock(block, "ruby", state = null).shouldBeFalse()

        verify(exactly = 0) { nexo.placeBlock(any(), any()) }
    }

    @Test
    fun `when break block - should remove block and drop consumed items`() {
        every { nexo.getDrops(block, tool) } returns listOf(ruby)
        every { nexo.removeBlock(block) } returns true

        provider.breakBlock(block, tool, DropsConsumer.drop()).shouldBeTrue()

        verifyOrder {
            nexo.getDrops(block, tool)
            nexo.removeBlock(block)
            blockWorld.dropItemNaturally(blockLocation, ruby)
        }
    }

    @Test
    fun `when break block and drops disappear - should drop nothing`() {
        every { nexo.getDrops(block, null) } returns listOf(ruby)
        every { nexo.removeBlock(block) } returns true

        provider.breakBlock(block, tool = null, DropsConsumer.disappear()).shouldBeTrue()

        verify(exactly = 0) { blockWorld.dropItemNaturally(any(), any()) }
    }

    @Test
    fun `when break not Nexo block - should return false`() {
        provider.breakBlock(block, tool, DropsConsumer.drop()).shouldBeFalse()

        verify(exactly = 0) { nexo.removeBlock(any()) }
    }

    @Test
    fun `when break block and Nexo doesn't remove it - should drop nothing`() {
        every { nexo.getDrops(block, tool) } returns listOf(ruby)
        every { nexo.removeBlock(block) } returns false

        provider.breakBlock(block, tool, DropsConsumer.drop()).shouldBeFalse()

        verify(exactly = 0) { blockWorld.dropItemNaturally(any(), any()) }
    }
}
