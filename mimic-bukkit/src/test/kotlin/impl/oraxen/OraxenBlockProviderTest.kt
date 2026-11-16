// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.oraxen

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
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
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

internal class OraxenBlockProviderTest : BukkitTestBase() {

    private val blocks = setOf("ruby_block", "Ruby Ore")

    private val world: World = mockk {
        every { dropItemNaturally(any(), any()) } returns mockk()
    }
    private val block: Block = mockk {
        every { world } returns this@OraxenBlockProviderTest.world
        every { location } returns Location(this@OraxenBlockProviderTest.world, 0.0, 0.0, 0.0)
    }

    private val oraxen: OraxenWrapper = mockk {
        every { blockIds } returns blocks
        every { isBlock(any()) } answers { firstArg() in blocks }
        every { getItemId(any()) } returns null
        every { getBlockId(any()) } returns null
        every { getDrops(any(), any()) } returns null
        every { createItem(any()) } answers { ItemStack(Material.PAPER) }
        every { placeBlock(any(), any()) } returns true
        every { removeBlock(any()) } returns true
    }

    // SUT
    private val provider = OraxenBlockProvider(oraxen)

    @Test
    fun `when get all blocks - should return converted ids`() {
        provider.allBlocks.toList() shouldContainExactly listOf("ruby_block", "ruby_ore")
    }

    @Test
    fun `when two ids become the same - should keep the id needing no conversion`() {
        every { oraxen.blockIds } returns setOf("Ruby_Block", "ruby_block")

        provider.allBlocks.toList() shouldContainExactly listOf("ruby_block")
        provider.setBlock(block, "ruby_block", state = null).shouldBeTrue()
        verify { oraxen.placeBlock(block, "ruby_block") }
    }

    @Test
    fun `when get all blocks - every block should exist`() {
        provider.allBlocks.all(provider::hasBlock).shouldBeTrue()
    }

    @Test
    fun `when check block exists - should find it by converted id`() {
        provider.hasBlock("ruby_ore").shouldBeTrue()
        provider.hasBlock("unknown").shouldBeFalse()
    }

    @Test
    fun `when get block id - should return converted id`() {
        every { oraxen.getBlockId(block) } returns "Ruby Ore"

        provider.getBlockId(block) shouldBe "ruby_ore"
    }

    @Test
    fun `when get id of not Oraxen block - should return null`() {
        provider.getBlockId(block).shouldBeNull()
    }

    @Test
    fun `when get block id from block item - should return converted id`() {
        every { oraxen.getItemId(any()) } returns "Ruby Ore"

        provider.getBlockIdFromItem(ItemStack(Material.PAPER)) shouldBe "ruby_ore"
    }

    @Test
    fun `when get block id from item placing no block - should return null`() {
        every { oraxen.getItemId(any()) } returns "ruby_sword"

        provider.getBlockIdFromItem(ItemStack(Material.PAPER)).shouldBeNull()
    }

    @Test
    fun `when create block item - should create item by original id`() {
        val item = provider.createBlockItem("ruby_ore", amount = 3)

        item?.amount shouldBe 3
        verify { oraxen.createItem("Ruby Ore") }
    }

    @Test
    fun `when create item of unknown block - should return null`() {
        provider.createBlockItem("unknown", amount = 1).shouldBeNull()
    }

    @Test
    fun `when set block - should place block by original id`() {
        provider.setBlock(block, "ruby_ore", state = null).shouldBeTrue()

        verify { oraxen.placeBlock(block, "Ruby Ore") }
    }

    @Test
    fun `when set block with state or unknown block - should not place block`() {
        provider.setBlock(block, "ruby_ore", state = "[facing=north]").shouldBeFalse()
        provider.setBlock(block, "unknown", state = null).shouldBeFalse()

        verify(exactly = 0) { oraxen.placeBlock(any(), any()) }
    }

    @Test
    fun `when break block - should remove block and drop consumed items`() {
        val tool = ItemStack(Material.DIAMOND_PICKAXE)
        every { oraxen.getDrops(block, any()) } returns listOf(ItemStack(Material.DIAMOND), ItemStack(Material.STICK))

        provider.breakBlock(block, tool) { drops -> drops.filter { it.type == Material.DIAMOND } }.shouldBeTrue()

        verify { oraxen.removeBlock(block) }
        val dropped = mutableListOf<ItemStack>()
        verify { world.dropItemNaturally(any(), capture(dropped)) }
        dropped.map { it.type } shouldContainExactly listOf(Material.DIAMOND)
    }

    @Test
    fun `when break not Oraxen block - should not remove block`() {
        provider.breakBlock(block, tool = null) { it }.shouldBeFalse()

        verify(exactly = 0) { oraxen.removeBlock(any()) }
    }
}
