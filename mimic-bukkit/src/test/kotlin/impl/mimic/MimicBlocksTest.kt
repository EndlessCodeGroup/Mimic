// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mimic

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.ServicePriority.*
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.blocks.BlockProvider
import ru.endlesscode.mimic.blocks.Blocks
import ru.endlesscode.mimic.blocks.DropsConsumer
import ru.endlesscode.mimic.internal.Log
import kotlin.test.Test

internal class MimicBlocksTest : BukkitTestBase() {

    private val minecraft = TestBlockProvider()
    private val custom = CustomBlockProvider()

    private var disabledProviders = emptySet<String>()
    private val blockProviders = ServiceRegistry<BlockProvider>(
        "BlockProvider", plugin, defaultId = "minecraft",
    ) { disabledProviders }

    // SUT
    private val blocks: Blocks = MimicBlocks(blockProviders)

    init {
        blockProviders.register(minecraft, plugin, Lowest)
        blockProviders.register(custom, plugin, Normal)
    }

    @Test
    fun `when get all blocks - should return namespaced blocks of all providers`() {
        blocks.getAllBlocks().map { it.asString() } shouldContainExactly listOf(
            "custom:ruby_block",
            "custom:default/palm_log",
            "minecraft:stone",
            "minecraft:note_block",
            "minecraft:wheat",
            "minecraft:bedrock",
        )
    }

    @Test
    fun `when get all blocks - should skip invalid and duplicated paths`() {
        blockProviders.register(InvalidIdBlockProvider(), plugin, High)

        val invalidBlocks = blocks.getAllBlocks().filter { it.namespace() == "invalid" }
        invalidBlocks.map { it.asString() } shouldContainExactly listOf("invalid:emerald_block")
    }

    @ParameterizedTest
    @CsvSource(
        "minecraft:stone,               true",
        "ns:stone,                      false",
        "minecraft:unknown,             false",
        "minecraft:golden_sword,        false",
        "custom:default/palm_log,       true",
        "custom:stone,                  false",
        "minecraft:default/palm_log,    false",
        "minecraft:ruby_block,          false",
    )
    fun `when check has block`(blockId: String, shouldExist: Boolean) {
        blocks.hasBlock(Key.key(blockId)) shouldBe shouldExist
    }

    @Test
    fun `when provider can't list its blocks - should ask it whether a block exists`() {
        blockProviders.register(UnlistedBlockProvider(), plugin, Normal)

        assertSoftly {
            blocks.hasBlock(Key.key("unlisted:ruby_block")).shouldBeTrue()
            blocks.hasBlock(Key.key("unlisted:stone")).shouldBeFalse()
            blocks.getAllBlocks().filter { it.namespace() == "unlisted" }.shouldBeEmpty()
        }
    }

    @ParameterizedTest
    @CsvSource(
        "STONE,         minecraft:stone",
        "NOTE_BLOCK,    custom:ruby_block",
    )
    fun `when get block id - should return id of the provider with the highest priority`(
        material: Material,
        expectedId: String,
    ) {
        blocks.getBlockId(mockBlock(material)) shouldBe Key.key(expectedId)
    }

    @ParameterizedTest
    @CsvSource(
        "STONE,         minecraft:stone,        true",
        "STONE,         minecraft:bedrock,      false",
        "NOTE_BLOCK,    custom:ruby_block,      true",
        "NOTE_BLOCK,    minecraft:note_block,   false",
        "NOTE_BLOCK,    custom:note_block,      false",
    )
    fun `when check is block - should compare with id of the owner`(
        material: Material,
        blockId: String,
        shouldBeSame: Boolean,
    ) {
        blocks.isBlock(mockBlock(material), Key.key(blockId)) shouldBe shouldBeSame
    }

    @ParameterizedTest
    @CsvSource(
        "STONE,         minecraft:stone",
        "WHEAT_SEEDS,   minecraft:wheat",
        "EMERALD,       custom:ruby_block",
    )
    fun `when get block id from item - should return id`(material: Material, expectedId: String) {
        blocks.getBlockIdFromItem(ItemStack(material)) shouldBe Key.key(expectedId)
    }

    @Test
    fun `when get block id from non-block item - should return null`() {
        blocks.getBlockIdFromItem(ItemStack(Material.GOLDEN_SWORD)).shouldBeNull()
    }

    @ParameterizedTest
    @CsvSource(
        "minecraft:stone,   STONE",
        "minecraft:wheat,   WHEAT_SEEDS",
        "custom:ruby_block, EMERALD",
    )
    fun `when create block item - should return item from the provider`(blockId: String, expected: Material) {
        blockProviders.register(FailingBlockProvider(), plugin, High)

        val item = blocks.createBlockItem(Key.key(blockId), amount = 5)
        assertNotNull(item, "Item should not be null")
        // Not assertSoftly(item), it prints ItemStack requiring ItemFactory which isn't mocked
        assertSoftly {
            item.type shouldBe expected
            item.amount shouldBe 5
        }
    }

    @ParameterizedTest
    @CsvSource(
        "0,     1",
        "-5,    1",
        "64,    64",
        "100,   64",
    )
    fun `when create block item - should keep amount within stack size`(amount: Int, expectedAmount: Int) {
        val item = blocks.createBlockItem(Key.key("minecraft:stone"), amount)

        assertNotNull(item, "Item should not be null")
        item.amount shouldBe expectedAmount
    }

    @ParameterizedTest
    @ValueSource(strings = ["minecraft:unknown", "custom:stone", "minecraft:bedrock", "ns:stone", "failing:stone"])
    fun `when create item of unknown block or block without item - should return null`(blockId: String) {
        blockProviders.register(FailingBlockProvider(), plugin, High)

        blocks.createBlockItem(Key.key(blockId), amount = 1).shouldBeNull()
    }

    @Test
    fun `when set block - should set block with state`() {
        val block = mockBlock(Material.AIR)

        blocks.setBlock(block, Key.key("minecraft:stone"), "[facing=north]").shouldBeTrue()
        assertSoftly {
            verify { block.type = Material.STONE }
            minecraft.lastState shouldBe "[facing=north]"
        }
    }

    @Test
    fun `when set custom block - should set block`() {
        val block = mockBlock(Material.AIR)

        blocks.setBlock(block, Key.key("custom:default/palm_log"), state = null).shouldBeTrue()
        verify { block.type = Material.NOTE_BLOCK }
    }

    @ParameterizedTest
    @ValueSource(strings = ["ns:stone", "minecraft:unknown", "minecraft:bedrock", "minecraft:default/palm_log", "failing:stone"])
    fun `when set unknown block or setting failed - should return false`(blockId: String) {
        blockProviders.register(FailingBlockProvider(), plugin, High)
        val block = mockBlock(Material.AIR)

        blocks.setBlock(block, Key.key(blockId), state = null).shouldBeFalse()
        verify(exactly = 0) { block.type = any() }
    }

    @Test
    fun `when set block in place of non-empty block - should not ask the provider`() {
        val block = mockBlock(Material.DIRT)

        blocks.setBlock(block, Key.key("minecraft:stone"), "[facing=north]").shouldBeFalse()
        assertSoftly {
            verify(exactly = 0) { block.type = any() }
            minecraft.lastState.shouldBeNull()
        }
    }

    @Test
    fun `when break block - should pass tool and drop items on the ground`() {
        val block = mockBlock(Material.STONE)
        val tool = ItemStack(Material.DIAMOND_PICKAXE)

        blocks.breakBlock(block, tool, DropsConsumer.drop()).shouldBeTrue()
        assertSoftly {
            verify { block.type = Material.AIR }
            minecraft.lastTool shouldBeSameInstanceAs tool
            minecraft.droppedOnGround.map { it.type } shouldContainExactly listOf(Material.STONE)
        }
    }

    @Test
    fun `when use short forms - should use defaults`() {
        assertSoftly {
            blocks.createBlockItem(Key.key("minecraft:stone"))?.amount shouldBe 1
            blocks.setBlock(mockBlock(Material.AIR), Key.key("minecraft:stone")).shouldBeTrue()
            minecraft.lastState.shouldBeNull()
            blocks.breakBlock(mockBlock(Material.STONE)).shouldBeTrue()
            minecraft.lastTool.shouldBeNull()
            minecraft.droppedOnGround.map { it.type } shouldContainExactly listOf(Material.STONE)
        }
    }

    @Test
    fun `when break block with drops consumer - should pass drops to consumer`() {
        val block = mockBlock(Material.STONE)
        val consumedDrops = mutableListOf<ItemStack>()

        blocks.breakBlock(block, tool = null) { drops -> consumedDrops += drops; emptyList() }.shouldBeTrue()
        assertSoftly {
            consumedDrops.map { it.type } shouldContainExactly listOf(Material.STONE)
            minecraft.droppedOnGround.shouldBeEmpty()
        }
    }

    @Test
    fun `when drops consumer fails - should drop all items on the ground`() {
        blocks.breakBlock(mockBlock(Material.STONE), tool = null) { error("Failure") }.shouldBeTrue()
        minecraft.droppedOnGround.map { it.type } shouldContainExactly listOf(Material.STONE)
    }

    @Test
    fun `when break block with disappearing drops - should not drop items`() {
        blocks.breakBlock(mockBlock(Material.STONE), tool = null, DropsConsumer.disappear()).shouldBeTrue()
        minecraft.droppedOnGround.shouldBeEmpty()
    }

    @Test
    fun `when break custom block - should break it only via the owner`() {
        blocks.breakBlock(mockBlock(Material.NOTE_BLOCK), tool = null, DropsConsumer.drop()).shouldBeTrue()
        assertSoftly {
            custom.droppedOnGround.map { it.type } shouldContainExactly listOf(Material.EMERALD)
            minecraft.droppedOnGround.shouldBeEmpty()
        }
    }

    @Test
    fun `when break air - should return false`() {
        val block = mockBlock(Material.AIR)

        blocks.breakBlock(block, tool = null, DropsConsumer.drop()).shouldBeFalse()
        verify(exactly = 0) { block.type = any() }
    }

    @Test
    fun `when provider is disabled in config - should ignore it`() {
        disabledProviders = setOf(custom.id)

        assertSoftly {
            blocks.hasBlock(Key.key("custom:ruby_block")).shouldBeFalse()
            blocks.getBlockId(mockBlock(Material.NOTE_BLOCK)) shouldBe Key.key("minecraft:note_block")
        }
    }

    @Test
    fun `when provider fails to identify block - should not fall back to the default provider`() {
        blockProviders.register(FailingBlockProvider(), plugin, High)
        val block = mockBlock(Material.STONE)

        assertSoftly {
            blocks.getBlockId(block).shouldBeNull()
            blocks.isBlock(block, Key.key("minecraft:stone")).shouldBeFalse()
            blocks.breakBlock(block, tool = null, DropsConsumer.drop()).shouldBeFalse()
            minecraft.droppedOnGround.shouldBeEmpty()
        }
    }

    @Test
    fun `when provider fails to identify block - should fall back to other custom providers`() {
        blockProviders.register(FailingBlockProvider(), plugin, High)

        blocks.getBlockId(mockBlock(Material.NOTE_BLOCK)) shouldBe Key.key("custom:ruby_block")
    }

    @Test
    fun `when provider fails - should return defaults`() {
        blockProviders.register(FailingBlockProvider(), plugin, High)

        assertSoftly {
            blocks.getAllBlocks().filter { it.namespace() == "failing" }.shouldBeEmpty()
            blocks.hasBlock(Key.key("failing:stone")).shouldBeFalse()
            blocks.getBlockIdFromItem(ItemStack(Material.GOLDEN_SWORD)).shouldBeNull()
        }
    }

    @Test
    fun `when provider returns invalid id - should ignore it`() {
        blockProviders.register(InvalidIdBlockProvider(), plugin, High)

        assertSoftly {
            // Should not fall back to the default provider
            blocks.getBlockId(mockBlock(Material.STONE)).shouldBeNull()
            blocks.getBlockId(mockBlock(Material.NOTE_BLOCK)) shouldBe Key.key("custom:ruby_block")
        }
    }

    @Test
    fun `when minecraft block is unknown - should warn once only if other provider has it`() {
        val warnings = mutableListOf<String>()
        Log.init({ _, message, _ -> warnings += message.orEmpty() })

        repeat(2) {
            blocks.hasBlock(Key.key("minecraft:ruby_block")).shouldBeFalse()
            blocks.hasBlock(Key.key("minecraft:unknown")).shouldBeFalse()
        }

        warnings.single() shouldContain "'minecraft:ruby_block' doesn't exist"
        warnings.single() shouldContain "Did you mean 'custom:ruby_block'?"
    }

    private fun mockBlock(material: Material): Block = mockk(relaxUnitFun = true) {
        every { type } returns material
        every { isEmpty } returns (material == Material.AIR)
    }
}

/** Provider imitating vanilla blocks. */
private class TestBlockProvider : BlockProvider {
    override val id = "minecraft"

    // Material.isBlock requires a server, so the known blocks are listed explicitly
    override val allBlocks = listOf(Material.STONE, Material.NOTE_BLOCK, Material.WHEAT, Material.BEDROCK)
        .map { it.name.lowercase() }

    val droppedOnGround = mutableListOf<ItemStack>()
    var lastState: String? = null
    var lastTool: ItemStack? = null

    override fun getBlockId(block: Block): String? = block.type.name.lowercase().takeIf { it in allBlocks }

    override fun getBlockIdFromItem(item: ItemStack): String? = when (item.type) {
        Material.WHEAT_SEEDS -> "wheat"
        else -> item.type.name.lowercase().takeIf { it in allBlocks }
    }

    override fun createBlockItem(blockId: String, amount: Int): ItemStack? = when (blockId) {
        "wheat" -> ItemStack(Material.WHEAT_SEEDS, amount)
        "bedrock" -> null
        else -> if (blockId in allBlocks) ItemStack(Material.valueOf(blockId.uppercase()), amount) else null
    }

    override fun setBlock(block: Block, blockId: String, state: String?): Boolean {
        lastState = state
        if (blockId !in allBlocks || blockId == "bedrock") return false
        block.type = Material.valueOf(blockId.uppercase())
        return true
    }

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer): Boolean {
        lastTool = tool
        if (block.type == Material.AIR) return false
        droppedOnGround += dropsConsumer.consume(listOf(ItemStack(block.type)))
        block.type = Material.AIR
        return true
    }
}

/** Provider imitating a plugin with custom blocks using note block under the hood. */
private class CustomBlockProvider : BlockProvider {
    override val id = "custom"
    override val allBlocks = listOf("ruby_block", "default/palm_log")

    val droppedOnGround = mutableListOf<ItemStack>()

    override fun getBlockId(block: Block): String? = "ruby_block".takeIf { block.type == Material.NOTE_BLOCK }

    override fun getBlockIdFromItem(item: ItemStack): String? = "ruby_block".takeIf { item.type == Material.EMERALD }

    override fun createBlockItem(blockId: String, amount: Int): ItemStack? =
        if (blockId == "ruby_block") ItemStack(Material.EMERALD, amount) else null

    override fun setBlock(block: Block, blockId: String, state: String?): Boolean {
        if (blockId !in allBlocks) return false
        block.type = Material.NOTE_BLOCK
        return true
    }

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer): Boolean {
        if (getBlockId(block) == null) return false
        droppedOnGround += dropsConsumer.consume(listOf(ItemStack(Material.EMERALD)))
        block.type = Material.AIR
        return true
    }
}

/** Provider imitating a plugin which can't list its blocks. */
private class UnlistedBlockProvider : BlockProvider {
    override val id = "unlisted"
    override val allBlocks = emptyList<String>()

    override fun hasBlock(blockId: String): Boolean = blockId == "ruby_block"
    override fun getBlockId(block: Block): String? = null
    override fun getBlockIdFromItem(item: ItemStack): String? = null
    override fun createBlockItem(blockId: String, amount: Int): ItemStack? = null
    override fun setBlock(block: Block, blockId: String, state: String?): Boolean = false
    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer): Boolean = false
}

/** Provider returning invalid IDs. */
private class InvalidIdBlockProvider : BlockProvider {
    override val id = "invalid"
    override val allBlocks = listOf("Ruby Block", "emerald_block", "ns:block", "", "emerald_block")

    override fun getBlockId(block: Block): String = allBlocks.first()
    override fun getBlockIdFromItem(item: ItemStack): String = allBlocks.first()
    override fun createBlockItem(blockId: String, amount: Int): ItemStack? = null
    override fun setBlock(block: Block, blockId: String, state: String?): Boolean = false
    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer): Boolean = false
}

/** Provider failing on every call. */
private class FailingBlockProvider : BlockProvider {
    override val id = "failing"
    override val allBlocks: List<String> get() = error("Failure")

    override fun getBlockId(block: Block): String = error("Failure")
    override fun getBlockIdFromItem(item: ItemStack): String = error("Failure")
    override fun createBlockItem(blockId: String, amount: Int): ItemStack = error("Failure")
    override fun setBlock(block: Block, blockId: String, state: String?): Boolean = error("Failure")
    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer): Boolean =
        error("Failure")
}
