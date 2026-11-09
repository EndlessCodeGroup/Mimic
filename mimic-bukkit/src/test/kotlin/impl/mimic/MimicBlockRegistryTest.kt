package ru.endlesscode.mimic.impl.mimic

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.ServicePriority.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.blocks.BukkitBlockRegistry
import ru.endlesscode.mimic.blocks.DropsConsumer
import ru.endlesscode.mimic.blocks.WrappedBlockRegistry
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.internal.parseKey
import kotlin.test.Test

@OptIn(ExperimentalMimicApi::class)
internal class MimicBlockRegistryTest : BukkitTestBase() {

    private val testRegistry = TestBlockRegistry()
    private val customRegistry = CustomBlockRegistry()

    // SUT
    private val blockRegistry: BukkitBlockRegistry = MimicBlockRegistry(servicesManager, parseKey = plugin::parseKey)

    init {
        servicesManager.register(BukkitBlockRegistry::class.java, testRegistry, plugin, Lowest)
        servicesManager.register(BukkitBlockRegistry::class.java, customRegistry, plugin, Normal)
        servicesManager.register(BukkitBlockRegistry::class.java, blockRegistry, plugin, Highest)
    }

    // Services manager is shared between tests
    @AfterEach
    fun tearDown() {
        servicesManager.unregisterAll(plugin)
    }

    @ParameterizedTest
    @ValueSource(strings = ["stone", "minecraft:stone", "mimic:stone"])
    fun `when check is same block - should return true`(blockId: String) {
        blockRegistry.isSameBlock(mockBlock(Material.STONE), blockId).shouldBeTrue()
    }

    @ParameterizedTest
    @CsvSource(
        "custom:ruby_block,      true",
        "ruby_block,             true",
        "mimic:ruby_block,       true",
        "note_block,             false",
        "minecraft:note_block,   false",
    )
    fun `when check is same block for custom block - should check the custom block`(
        blockId: String,
        shouldBeSame: Boolean,
    ) {
        blockRegistry.isSameBlock(mockBlock(Material.NOTE_BLOCK), blockId) shouldBe shouldBeSame
    }

    @ParameterizedTest
    @ValueSource(strings = ["stone", "minecraft:stone", "mimic:stone"])
    fun `when check is same block via wrapper - should return true`(blockId: String) {
        assertSoftly(wrap(blockRegistry)) {
            isSameBlock(mockBlock(Material.STONE), blockId).shouldBeTrue()
            isSameBlockItem(ItemStack(Material.STONE), blockId).shouldBeTrue()
        }
    }

    @ParameterizedTest
    @CsvSource(
        "STONE,         stone,                  true",
        "STONE,         minecraft:stone,        true",
        "WHEAT_SEEDS,   wheat,                  true",
        "EMERALD,       custom:ruby_block,      true",
        "EMERALD,       ruby_block,             true",
        "EMERALD,       minecraft:ruby_block,   false",
        "EMERALD,       custom:emerald_block,   false",
        "GOLDEN_SWORD,  golden_sword,           false",
    )
    fun `when check is same block item`(material: Material, blockId: String, shouldBeSame: Boolean) {
        blockRegistry.isSameBlockItem(ItemStack(material), blockId) shouldBe shouldBeSame
    }

    @ParameterizedTest
    @CsvSource(
        "STONE,         minecraft:stone",
        "NOTE_BLOCK,    custom:ruby_block",
    )
    fun `when get id - should return id of the registry with the highest priority`(
        material: Material,
        expectedId: String,
    ) {
        blockRegistry.getBlockId(mockBlock(material)) shouldBe expectedId
    }

    @ParameterizedTest
    @CsvSource(
        "STONE,         minecraft:stone",
        "WHEAT_SEEDS,   minecraft:wheat",
        "EMERALD,       custom:ruby_block",
    )
    fun `when get block id from item - should return id`(material: Material, expectedId: String) {
        blockRegistry.getBlockIdFromItem(ItemStack(material)) shouldBe expectedId
    }

    @Test
    fun `when get block id from non-block item - should return null`() {
        blockRegistry.getBlockIdFromItem(ItemStack(Material.GOLDEN_SWORD)).shouldBeNull()
    }

    @ParameterizedTest
    @CsvSource(
        "stone,                         true",
        "minecraft:stone,               true",
        "Stone,                         false",
        "ns:stone,                      false",
        "unknown,                       false",
        "golden_sword,                  false",
        "custom:default/palm_log,       true",
        "default/palm_log,              true",
        "mimic:default/palm_log,        true",
        "minecraft:default/palm_log,    false",
        "custom:default:palm_log,       false",
    )
    fun `when check is block exists`(blockId: String, shouldExist: Boolean) {
        blockRegistry.isBlockExists(blockId) shouldBe shouldExist
    }

    @ParameterizedTest
    @ValueSource(strings = ["stone", "minecraft:stone", "mimic:stone"])
    fun `when set block - should set block`(blockId: String) {
        val block = mockBlock(Material.AIR)

        blockRegistry.setBlock(block, blockId).shouldBeTrue()
        verify { block.type = Material.STONE }
    }

    @ParameterizedTest
    @ValueSource(strings = ["custom:default/palm_log", "default/palm_log", "mimic:default/palm_log"])
    fun `when set custom block - should set block`(blockId: String) {
        val block = mockBlock(Material.AIR)

        blockRegistry.setBlock(block, blockId).shouldBeTrue()
        verify { block.type = Material.NOTE_BLOCK }
    }

    @ParameterizedTest
    @ValueSource(strings = ["ns:stone", "minecraft:unknown", "minecraft:bedrock", "custom:default:palm_log"])
    fun `when set unknown block or setting failed - should return false`(blockId: String) {
        val block = mockBlock(Material.AIR)

        blockRegistry.setBlock(block, blockId).shouldBeFalse()
        verify(exactly = 0) { block.type = any() }
    }

    @Test
    fun `when set block in place of non-empty block - should return false`() {
        val block = mockBlock(Material.DIRT)

        blockRegistry.setBlock(block, "stone").shouldBeFalse()
        verify(exactly = 0) { block.type = any() }
    }

    @Test
    fun `when set block with payload via wrapper - should pass payload to the registry`() {
        wrap(blockRegistry).setBlock(mockBlock(Material.AIR), "stone", "[facing=north]").shouldBeTrue()
        testRegistry.lastPayload shouldBe "[facing=north]"
    }

    @Test
    fun `when break block - should drop items on the ground`() {
        val block = mockBlock(Material.STONE)

        blockRegistry.breakBlock(block).shouldBeTrue()
        assertSoftly {
            verify { block.type = Material.AIR }
            testRegistry.droppedOnGround.map { it.type } shouldContainExactly listOf(Material.STONE)
        }
    }

    @Test
    fun `when break block with drops consumer - should pass drops to consumer`() {
        val block = mockBlock(Material.STONE)
        val consumedDrops = mutableListOf<ItemStack>()

        blockRegistry.breakBlock(block, tool = null) { drops -> consumedDrops += drops; emptyList() }.shouldBeTrue()
        assertSoftly {
            consumedDrops.map { it.type } shouldContainExactly listOf(Material.STONE)
            testRegistry.droppedOnGround.shouldBeEmpty()
        }
    }

    @Test
    fun `when registry is disabled in config - should ignore it`() {
        servicesManager.unregister(customRegistry)
        servicesManager.register(
            BukkitBlockRegistry::class.java,
            wrap(customRegistry, disabledRegistries = setOf(customRegistry.id)),
            plugin,
            Normal,
        )

        assertSoftly {
            blockRegistry.isBlockExists("custom:ruby_block").shouldBeFalse()
            blockRegistry.getBlockId(mockBlock(Material.NOTE_BLOCK)) shouldBe "minecraft:note_block"
        }
    }

    @Test
    fun `when break block with disappearing drops - should not drop items`() {
        blockRegistry.breakBlock(mockBlock(Material.STONE), tool = null, DropsConsumer.disappear()).shouldBeTrue()
        testRegistry.droppedOnGround.shouldBeEmpty()
    }

    @Test
    fun `when break custom block - should break it only via the custom registry`() {
        blockRegistry.breakBlock(mockBlock(Material.NOTE_BLOCK)).shouldBeTrue()
        assertSoftly {
            customRegistry.droppedOnGround.map { it.type } shouldContainExactly listOf(Material.EMERALD)
            testRegistry.droppedOnGround.shouldBeEmpty()
        }
    }

    @Test
    fun `when break block with tool via wrapper - should pass tool to the registry`() {
        val tool = ItemStack(Material.DIAMOND_PICKAXE)

        wrap(blockRegistry).breakBlock(mockBlock(Material.STONE), tool).shouldBeTrue()
        testRegistry.lastTool shouldBeSameInstanceAs tool
    }

    @Test
    fun `when registry fails to identify block - should not fall back to the vanilla registry`() {
        servicesManager.register(BukkitBlockRegistry::class.java, FailingBlockRegistry(), plugin, High)
        val block = mockBlock(Material.STONE)

        assertSoftly {
            blockRegistry.getBlockId(block).shouldBeNull()
            blockRegistry.isSameBlock(block, "stone").shouldBeFalse()
            blockRegistry.breakBlock(block).shouldBeFalse()
            testRegistry.droppedOnGround.shouldBeEmpty()
        }
    }

    @Test
    fun `when registry fails to identify block - should fall back to other custom registries`() {
        servicesManager.register(BukkitBlockRegistry::class.java, FailingBlockRegistry(), plugin, High)

        blockRegistry.getBlockId(mockBlock(Material.NOTE_BLOCK)) shouldBe "custom:ruby_block"
    }

    @Test
    fun `when break air - should return false`() {
        val block = mockBlock(Material.AIR)

        blockRegistry.breakBlock(block).shouldBeFalse()
        verify(exactly = 0) { block.type = any() }
    }

    private fun mockBlock(material: Material): Block = mockk(relaxUnitFun = true) {
        every { type } returns material
        every { isEmpty } returns (material == Material.AIR)
    }

    private fun wrap(
        registry: BukkitBlockRegistry,
        disabledRegistries: Set<String> = emptySet(),
    ): BukkitBlockRegistry {
        val pluginManager = mockk<PluginManager> {
            every { isPluginEnabled(any<String>()) } returns true
        }
        val config = mockk<MimicConfig> {
            every { disabledBlockRegistries } returns disabledRegistries
        }
        return WrappedBlockRegistry(registry, config, "Mimic", pluginManager)
    }
}

/** Registry imitating vanilla blocks. */
@OptIn(ExperimentalMimicApi::class)
private class TestBlockRegistry : BukkitBlockRegistry {
    override val id = "minecraft"

    // Material.isBlock requires a server, so the known blocks are listed explicitly
    override val knownIds = listOf(Material.STONE, Material.NOTE_BLOCK, Material.WHEAT, Material.BEDROCK)
        .map { it.name.lowercase() }

    val droppedOnGround = mutableListOf<ItemStack>()
    var lastPayload: Any? = null
    var lastTool: ItemStack? = null

    override fun isBlockExists(blockId: String): Boolean = blockId in knownIds

    override fun getBlockId(block: Block): String? = block.type.name.lowercase().takeIf(::isBlockExists)

    override fun getBlockIdFromItem(blockItem: ItemStack): String? = when (blockItem.type) {
        Material.WHEAT_SEEDS -> "wheat"
        else -> blockItem.type.name.lowercase().takeIf(::isBlockExists)
    }

    override fun setBlock(block: Block, blockId: String, payload: Any?): Boolean {
        lastPayload = payload
        if (!isBlockExists(blockId) || blockId == "bedrock") return false
        block.type = Material.valueOf(blockId.uppercase())
        return true
    }

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer<ItemStack>): Boolean {
        lastTool = tool
        if (block.type == Material.AIR) return false
        droppedOnGround += dropsConsumer.consume(listOf(ItemStack(block.type)))
        block.type = Material.AIR
        return true
    }
}

/** Registry imitating a plugin with custom blocks using note block under the hood. */
@OptIn(ExperimentalMimicApi::class)
private class CustomBlockRegistry : BukkitBlockRegistry {
    override val id = "custom"
    override val knownIds = listOf("ruby_block", "default/palm_log")

    val droppedOnGround = mutableListOf<ItemStack>()

    override fun isBlockExists(blockId: String): Boolean = blockId in knownIds

    override fun getBlockId(block: Block): String? = "ruby_block".takeIf { block.type == Material.NOTE_BLOCK }

    override fun getBlockIdFromItem(blockItem: ItemStack): String? =
        "ruby_block".takeIf { blockItem.type == Material.EMERALD }

    override fun setBlock(block: Block, blockId: String, payload: Any?): Boolean {
        if (!isBlockExists(blockId)) return false
        block.type = Material.NOTE_BLOCK
        return true
    }

    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer<ItemStack>): Boolean {
        if (getBlockId(block) == null) return false
        droppedOnGround += dropsConsumer.consume(listOf(ItemStack(Material.EMERALD)))
        block.type = Material.AIR
        return true
    }
}

/** Registry failing on every call. */
@OptIn(ExperimentalMimicApi::class)
private class FailingBlockRegistry : BukkitBlockRegistry {
    override val id = "failing"
    override val knownIds: List<String> get() = error("Failure")

    override fun isBlockExists(blockId: String): Boolean = error("Failure")
    override fun getBlockId(block: Block): String = error("Failure")
    override fun getBlockIdFromItem(blockItem: ItemStack): String = error("Failure")
    override fun setBlock(block: Block, blockId: String, payload: Any?): Boolean = error("Failure")
    override fun breakBlock(block: Block, tool: ItemStack?, dropsConsumer: DropsConsumer<ItemStack>): Boolean =
        error("Failure")
}
