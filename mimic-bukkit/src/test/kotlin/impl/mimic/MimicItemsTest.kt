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
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.ServicePriority.*
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.items.ItemProvider
import ru.endlesscode.mimic.items.Items
import kotlin.test.Test

internal class MimicItemsTest : BukkitTestBase() {

    private val minecraft = TestItemProvider()
    private val custom = CustomItemProvider()

    private var disabledProviders = emptySet<String>()
    private val itemProviders = ServiceRegistry<ItemProvider>(
        apiName = "ItemProvider",
        mimicPlugin = plugin,
        defaultId = "minecraft",
        disabledIds = { disabledProviders },
    )

    // SUT
    private val items: Items = MimicItems(itemProviders)

    init {
        itemProviders.register(minecraft, plugin, Lowest)
        itemProviders.register(custom, plugin, Normal)
    }

    @Test
    fun `when get all items - should return namespaced items of all providers`() {
        items.getAllItems().map { it.asString() } shouldContainExactly listOf(
            "custom:ruby",
            "custom:default/ruby_sword",
            "minecraft:stone",
            "minecraft:diamond",
            "minecraft:ender_pearl",
        )
    }

    @Test
    fun `when get all items - should skip invalid and duplicated paths`() {
        itemProviders.register(InvalidIdItemProvider(), plugin, High)

        val invalidItems = items.getAllItems().filter { it.namespace() == "invalid" }
        invalidItems.map { it.asString() } shouldContainExactly listOf("invalid:emerald")
    }

    @ParameterizedTest
    @CsvSource(
        "minecraft:stone,               true",
        "ns:stone,                      false",
        "mimic:stone,                   false",
        "minecraft:unknown,             false",
        "custom:default/ruby_sword,     true",
        "custom:stone,                  false",
        "minecraft:default/ruby_sword,  false",
        "minecraft:ruby,                false",
    )
    fun `when check has item`(itemId: String, shouldExist: Boolean) {
        items.hasItem(Key.key(itemId)) shouldBe shouldExist
    }

    @Test
    fun `when provider can't list its items - should ask it whether an item exists`() {
        itemProviders.register(UnlistedItemProvider(), plugin, Normal)

        assertSoftly {
            items.hasItem(Key.key("unlisted:ruby")).shouldBeTrue()
            items.hasItem(Key.key("unlisted:stone")).shouldBeFalse()
            items.getAllItems().filter { it.namespace() == "unlisted" }.shouldBeEmpty()
        }
    }

    @ParameterizedTest
    @CsvSource(
        "STONE,     minecraft:stone",
        "DIAMOND,   custom:ruby",
    )
    fun `when get item id - should return id of the provider with the highest priority`(
        material: Material,
        expectedId: String,
    ) {
        items.getItemId(ItemStack(material)) shouldBe Key.key(expectedId)
    }

    @Test
    fun `when get id of unknown item - should return null`() {
        items.getItemId(ItemStack(Material.AIR)).shouldBeNull()
    }

    // https://github.com/EndlessCodeGroup/Mimic/issues/86
    @ParameterizedTest
    @CsvSource(
        "STONE,     minecraft:stone,    true",
        "STONE,     minecraft:diamond,  false",
        "DIAMOND,   custom:ruby,        true",
        "DIAMOND,   minecraft:diamond,  false",
        "DIAMOND,   custom:diamond,     false",
    )
    fun `when check is item - should compare with id of the owner`(
        material: Material,
        itemId: String,
        shouldBeSame: Boolean,
    ) {
        items.isItem(ItemStack(material, 5), Key.key(itemId)) shouldBe shouldBeSame
    }

    @ParameterizedTest
    @CsvSource(
        "minecraft:stone,   STONE",
        "custom:ruby,       DIAMOND",
    )
    fun `when create item - should return item from the provider`(itemId: String, expected: Material) {
        itemProviders.register(FailingItemProvider(), plugin, High)

        val item = items.createItem(Key.key(itemId), amount = 5)
        assertNotNull(item, "Item should not be null")
        // Not assertSoftly(item), it prints ItemStack requiring ItemFactory which isn't mocked
        assertSoftly {
            item.type shouldBe expected
            item.amount shouldBe 5
        }
    }

    @ParameterizedTest
    @CsvSource(
        "minecraft:stone,       0,      1",
        "minecraft:stone,       -5,     1",
        "minecraft:stone,       64,     64",
        "minecraft:stone,       100,    64",
        "minecraft:ender_pearl, 64,     16",
    )
    fun `when create item - should keep amount within stack size`(itemId: String, amount: Int, expectedAmount: Int) {
        val item = items.createItem(Key.key(itemId), amount)

        assertNotNull(item, "Item should not be null")
        item.amount shouldBe expectedAmount
    }

    @ParameterizedTest
    @ValueSource(strings = ["minecraft:unknown", "custom:stone", "ns:stone", "mimic:stone", "failing:stone"])
    fun `when create unknown item - should return null`(itemId: String) {
        itemProviders.register(FailingItemProvider(), plugin, High)

        items.createItem(Key.key(itemId), amount = 1).shouldBeNull()
    }

    @Test
    fun `when create item - should pass player and payload only to the provider of the item`() {
        items.createItem(Key.key("minecraft:stone"), amount = 1, player, payload = "[damage=10]")

        assertSoftly {
            minecraft.lastPlayer shouldBeSameInstanceAs player
            minecraft.lastPayload shouldBe "[damage=10]"
            custom.createdItems shouldBe 0
        }
    }

    @Test
    fun `when use short forms - should use defaults`() {
        assertSoftly {
            items.createItem(Key.key("minecraft:stone"))?.amount shouldBe 1
            items.createItem(Key.key("minecraft:stone"), 5)?.amount shouldBe 5
            minecraft.lastPlayer.shouldBeNull()
            minecraft.lastPayload.shouldBeNull()

            items.createItem(Key.key("minecraft:stone"), 5, player)?.amount shouldBe 5
            minecraft.lastPlayer shouldBeSameInstanceAs player
            minecraft.lastPayload.shouldBeNull()

            items.createItem(Key.key("minecraft:stone"), payload = "[damage=10]")?.amount shouldBe 1
            minecraft.lastPlayer.shouldBeNull()
            minecraft.lastPayload shouldBe "[damage=10]"
        }
    }

    @Test
    fun `when provider can't parse payload - should return null and warn with id and payload once`() {
        val warnings = mutableListOf<String>()
        Log.init({ _, message, _ -> warnings += message.orEmpty() })

        repeat(2) {
            items.createItem(Key.key("minecraft:stone"), amount = 1, player = null, payload = "[invalid").shouldBeNull()
            // Unknown item is not a payload problem
            items.createItem(Key.key("minecraft:unknown"), amount = 1, player = null, payload = "[invalid").shouldBeNull()
        }

        warnings.single() shouldContain "'minecraft:stone'"
        warnings.single() shouldContain "'[invalid'"
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `when payload is blank - should pass null to the provider`(payload: String) {
        val item = items.createItem(Key.key("minecraft:stone"), amount = 1, player = null, payload)

        assertSoftly {
            item?.type shouldBe Material.STONE
            minecraft.lastPayload.shouldBeNull()
        }
    }

    @Test
    fun `when provider takes no payload - should ignore it`() {
        val item = items.createItem(Key.key("custom:ruby"), amount = 1, player = null, payload = "[invalid")

        item?.type shouldBe Material.DIAMOND
    }

    @Test
    fun `when create item twice - should return different stacks`() {
        val first = items.createItem(Key.key("minecraft:stone"))
        val second = items.createItem(Key.key("minecraft:stone"))

        first shouldNotBeSameInstanceAs second
    }

    @Test
    fun `when provider is disabled in config - should ignore it`() {
        disabledProviders = setOf(custom.id)

        assertSoftly {
            items.hasItem(Key.key("custom:ruby")).shouldBeFalse()
            items.createItem(Key.key("custom:ruby")).shouldBeNull()
            items.getItemId(ItemStack(Material.DIAMOND)) shouldBe Key.key("minecraft:diamond")
        }
    }

    @Test
    fun `when provider fails to identify item - should not fall back to the default provider`() {
        itemProviders.register(FailingItemProvider(), plugin, High)
        val item = ItemStack(Material.STONE)

        assertSoftly {
            items.getItemId(item).shouldBeNull()
            items.isItem(item, Key.key("minecraft:stone")).shouldBeFalse()
        }
    }

    @Test
    fun `when provider fails to identify item - should fall back to other custom providers`() {
        itemProviders.register(FailingItemProvider(), plugin, High)

        items.getItemId(ItemStack(Material.DIAMOND)) shouldBe Key.key("custom:ruby")
    }

    @Test
    fun `when provider fails - should return defaults`() {
        itemProviders.register(FailingItemProvider(), plugin, High)

        assertSoftly {
            items.getAllItems().filter { it.namespace() == "failing" }.shouldBeEmpty()
            items.hasItem(Key.key("failing:stone")).shouldBeFalse()
            items.createItem(Key.key("failing:stone")).shouldBeNull()
        }
    }

    @Test
    fun `when provider returns invalid id - should ignore it`() {
        itemProviders.register(InvalidIdItemProvider(), plugin, High)

        assertSoftly {
            // Should not fall back to the default provider
            items.getItemId(ItemStack(Material.STONE)).shouldBeNull()
            items.getItemId(ItemStack(Material.DIAMOND)) shouldBe Key.key("custom:ruby")
        }
    }

    @Test
    fun `when minecraft item is unknown - should warn once only if other provider has it`() {
        val warnings = mutableListOf<String>()
        Log.init({ _, message, _ -> warnings += message.orEmpty() })

        repeat(2) {
            items.hasItem(Key.key("minecraft:ruby")).shouldBeFalse()
            items.hasItem(Key.key("minecraft:unknown")).shouldBeFalse()
        }

        warnings.single() shouldContain "'minecraft:ruby' doesn't exist"
        warnings.single() shouldContain "Did you mean 'custom:ruby'?"
    }
}

/** Provider imitating vanilla items. Takes payload starting with `[`. */
private class TestItemProvider : ItemProvider {
    override val id = "minecraft"

    // Material.isItem requires a server, so the known items are listed explicitly
    override val allItems = listOf(Material.STONE, Material.DIAMOND, Material.ENDER_PEARL).map { it.name.lowercase() }

    var lastPlayer: Player? = null
    var lastPayload: String? = null

    override fun getItemId(item: ItemStack): String? = item.type.name.lowercase().takeIf { it in allItems }

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? {
        lastPlayer = player
        lastPayload = payload
        if (itemId !in allItems || payload == "[invalid") return null
        return ItemStack(Material.valueOf(itemId.uppercase()), amount)
    }
}

/** Provider imitating a plugin with custom items built on diamond. Takes no payload. */
private class CustomItemProvider : ItemProvider {
    override val id = "custom"
    override val allItems = listOf("ruby", "default/ruby_sword")

    var createdItems = 0

    override fun getItemId(item: ItemStack): String? = "ruby".takeIf { item.type == Material.DIAMOND }

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? {
        createdItems++
        return if (itemId == "ruby") ItemStack(Material.DIAMOND, amount) else null
    }
}

/** Provider imitating a plugin which can't list its items. */
private class UnlistedItemProvider : ItemProvider {
    override val id = "unlisted"
    override val allItems = emptyList<String>()

    override fun hasItem(itemId: String): Boolean = itemId == "ruby"
    override fun getItemId(item: ItemStack): String? = null
    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? = null
}

/** Provider returning invalid IDs. */
private class InvalidIdItemProvider : ItemProvider {
    override val id = "invalid"
    override val allItems = listOf("Ruby Sword", "emerald", "ns:item", "", "emerald")

    override fun getItemId(item: ItemStack): String = allItems.first()
    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? = null
}

/** Provider failing on every call. */
private class FailingItemProvider : ItemProvider {
    override val id = "failing"
    override val allItems: List<String> get() = error("Failure")

    override fun getItemId(item: ItemStack): String = error("Failure")
    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack =
        error("Failure")
}
