// Copyright (c) 2020 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // ItemsRegistry API is deprecated but still supported

package ru.endlesscode.mimic.impl.mimic

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.ServicePriority.Lowest
import org.bukkit.plugin.ServicePriority.Normal
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.items.ItemProvider
import ru.endlesscode.mimic.items.ItemsRegistryItemProvider
import kotlin.test.Test

internal class MimicItemsRegistryTest : BukkitTestBase() {

    private val providers = ServiceRegistry<ItemProvider>("ItemProvider", plugin, defaultId = "test")

    // SUT
    private val itemsService: BukkitItemsRegistry = MimicItemsRegistry(MimicItems(providers))

    init {
        providers.register(VanillaItemProvider(), plugin, Lowest)
    }

    @ParameterizedTest
    @ValueSource(strings = ["acacia_boat", "test:acacia_boat", "mimic:acacia_boat"])
    fun `when check is same item - should return true`(itemId: String) {
        val item = ItemStack(Material.ACACIA_BOAT)
        itemsService.isSameItem(item, itemId).shouldBeTrue()
    }

    // https://github.com/EndlessCodeGroup/Mimic/issues/86
    @ParameterizedTest
    @CsvSource(
        "test:emerald,  false",
        "emerald,       false",
        "custom:ruby,   true",
        "ruby,          true",
    )
    fun `when check is same item - should not match custom item with its vanilla base`(itemId: String, isSame: Boolean) {
        providers.register(RubyItemProvider(), plugin, Normal)

        itemsService.isSameItem(ItemStack(Material.EMERALD), itemId) shouldBe isSame
    }

    // https://github.com/EndlessCodeGroup/Mimic/issues/85
    @ParameterizedTest
    @CsvSource(
        "quantum:gems/ruby, true",
        "quantum:ruby,      true",
        "ruby,              true",
        "quantum:emerald,   false",
        "test:ruby,         false",
    )
    fun `when registry compares items in its own way - should use it`(itemId: String, isSame: Boolean) {
        registerRegistry(ModularItemRegistry())

        itemsService.isSameItem(ItemStack(Material.EMERALD), itemId) shouldBe isSame
    }

    @ParameterizedTest
    @ValueSource(strings = ["acacia_boat", "test:acacia_boat", "mimic:acacia_boat", "test:ACACIA_BOAT", "Test:acacia_boat"])
    fun `when get item - should return item stack`(itemId: String) {
        val item = itemsService.getItem(itemId).shouldNotBeNull()

        assertSoftly {
            item.type shouldBe Material.ACACIA_BOAT
            item.amount shouldBe 1
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["ns:acacia_boat", "test:unknown", "42", "test:acacia boat", ":acacia_boat", "test:"])
    fun `when get unknown item - should return null`(itemId: String) {
        itemsService.getItem(itemId).shouldBeNull()
    }

    @Test
    fun `when get item with payload - should pass string payload to provider`() {
        val provider = VanillaItemProvider()
        providers.register(provider, plugin, Lowest)

        assertSoftly {
            itemsService.getItem("test:stone", "[damage=10]", 5)?.amount shouldBe 5
            provider.lastPayload shouldBe "[damage=10]"
            // Item providers can't take a payload which isn't a string
            itemsService.getItem("test:stone", 42, 5).shouldBeNull()
        }
    }

    @Test
    fun `when get item from registry - should pass payload of any type and copy the stack`() {
        val registry = ModularItemRegistry()
        registerRegistry(registry)

        val item = itemsService.getItem("quantum:gems/ruby", 42, 5)

        assertSoftly {
            registry.lastPayload shouldBe 42
            item?.type shouldBe Material.EMERALD
            item shouldNotBeSameInstanceAs registry.sharedStack
        }
    }

    @Test
    fun `when get id - should return id`() {
        val item = ItemStack(Material.ACACIA_BOAT)
        itemsService.getItemId(item) shouldBe "test:acacia_boat"
    }

    @ParameterizedTest
    @CsvSource(
        "stone,         true",
        "ns:stone,      false",
        "test:stone,    true",
        "unknown,       false",
        "gold_sword,    false",
        "golden_sword,  true",
        "mimic:stone,   true",
        "test:STONE,    true",
        "test:stone!,   false",
    )
    fun `when check is item exists`(itemId: String, shouldExist: Boolean) {
        itemsService.isItemExists(itemId) shouldBe shouldExist
    }

    @ParameterizedTest
    @ValueSource(strings = ["test:acacia boat", ":acacia_boat", "test:", "test:acacia_boat!"])
    fun `when use invalid id - should not query providers`(itemId: String) {
        val provider = mockk<ItemProvider>(relaxed = true) {
            every { id } returns "mock"
            every { isEnabled } returns true
        }
        providers.register(provider, plugin, Normal)

        assertSoftly {
            itemsService.isItemExists(itemId) shouldBe false
            itemsService.getItem(itemId).shouldBeNull()
            itemsService.isSameItem(ItemStack(Material.ACACIA_BOAT), itemId) shouldBe false
        }
        verify(exactly = 0) {
            provider.allItems
            provider.createItem(any(), any(), any(), any())
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["ia:itemsadder:ruby_sword", "ia:itemsadder/ruby_sword", "mimic:itemsadder:ruby_sword"])
    fun `when registry uses ids with colon - should accept both forms`(itemId: String) {
        registerRegistry(LegacyItemRegistry())

        assertSoftly {
            itemsService.isItemExists(itemId).shouldBeTrue()
            itemsService.getItem(itemId)?.type shouldBe Material.EMERALD
            itemsService.isSameItem(ItemStack(Material.EMERALD), itemId).shouldBeTrue()
        }
    }

    @Test
    fun `when registry uses ids with colon - should return them with slash`() {
        registerRegistry(LegacyItemRegistry())

        assertSoftly {
            itemsService.knownIds shouldContain "ia:itemsadder/ruby_sword"
            itemsService.getItemId(ItemStack(Material.EMERALD)) shouldBe "ia:itemsadder/ruby_sword"
        }
    }

    @Test
    fun `when registry returns invalid id - should ignore it`() {
        registerRegistry(InvalidIdItemRegistry())

        assertSoftly {
            itemsService.knownIds shouldNotContain "invalid:Ruby Sword"
            itemsService.knownIds shouldContain "invalid:emerald"
            // Should not fall back to the provider with lower priority
            itemsService.getItemId(ItemStack(Material.EMERALD)).shouldBeNull()
        }
    }

    // https://github.com/EndlessCodeGroup/Mimic/issues/87
    @ParameterizedTest
    @ValueSource(strings = ["mmoitems:katana", "katana", "mmoitems:KATANA", "mmoitems:sword/katana"])
    fun `when MMOItems id has no type and is unique - should resolve it`(itemId: String) {
        providers.register(MmoItemProvider(), plugin, Normal)

        assertSoftly {
            itemsService.isItemExists(itemId).shouldBeTrue()
            itemsService.getItem(itemId)?.type shouldBe Material.IRON_SWORD
            itemsService.isSameItem(ItemStack(Material.IRON_SWORD), itemId).shouldBeTrue()
            itemsService.getItemId(ItemStack(Material.IRON_SWORD)) shouldBe "mmoitems:sword/katana"
        }
    }

    @Test
    fun `when MMOItems id has no type and is not unique - should not resolve it`() {
        providers.register(MmoItemProvider(), plugin, Normal)

        assertSoftly {
            itemsService.isItemExists("mmoitems:excalibur").shouldBeFalse()
            itemsService.getItem("mmoitems:excalibur").shouldBeNull()
        }
    }

    @Test
    fun `when MMOItems id has no type - should warn once`() {
        val warnings = mutableListOf<String>()
        Log.init({ _, message, _ -> warnings += message.orEmpty() })
        providers.register(MmoItemProvider(), plugin, Normal)

        repeat(2) {
            itemsService.getItem("mmoitems:katana")
            itemsService.getItem("mmoitems:excalibur")
            itemsService.getItem("mmoitems:sword/katana")
        }

        warnings.size shouldBe 2
        warnings[0] shouldContain "use 'mmoitems:sword/katana' instead"
        warnings[1] shouldContain "'mmoitems:sword/excalibur', 'mmoitems:dagger/excalibur'"
    }

    @Test
    fun `when ids are deprecated or invalid - should warn once`() {
        val warnings = mutableListOf<String>()
        Log.init({ _, message, _ -> warnings += message.orEmpty() })
        registerRegistry(LegacyItemRegistry())
        registerRegistry(InvalidIdItemRegistry())

        repeat(2) {
            itemsService.knownIds
            itemsService.getItem("ia:itemsadder:ruby_sword")
            itemsService.getItem("test:ACACIA_BOAT")
        }

        // The first quoted value is the provider ID for provider warnings, and the item ID for input warnings
        warnings.map { it.substringAfter("'").substringBefore("'") } shouldContainExactlyInAnyOrder listOf(
            "invalid",
            "ia:itemsadder:ruby_sword",
            "test:ACACIA_BOAT",
        )
    }

    private fun registerRegistry(registry: BukkitItemsRegistry) {
        providers.register(ItemsRegistryItemProvider(registry), plugin, Normal, isBridge = true)
    }
}

/** Provider imitating vanilla items. */
private class VanillaItemProvider : ItemProvider {
    override val id = "test"
    override val allItems = Material.entries.filterNot { it.isLegacy }.mapTo(HashSet()) { it.name.lowercase() }

    var lastPayload: String? = null

    override fun getItemId(item: ItemStack): String = item.type.name.lowercase()

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? {
        lastPayload = payload
        return if (itemId in allItems) ItemStack(Material.valueOf(itemId.uppercase()), amount) else null
    }
}

/** Provider imitating a plugin with a custom item built on emerald. */
private class RubyItemProvider : ItemProvider {
    override val id = "custom"
    override val allItems = listOf("ruby")

    override fun getItemId(item: ItemStack): String? = "ruby".takeIf { item.type == Material.EMERALD }

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? =
        if (itemId == "ruby") ItemStack(Material.EMERALD, amount) else null
}

/** Provider imitating MMOItems, where IDs are unique only within an item type. */
private class MmoItemProvider : ItemProvider {
    override val id = "mmoitems"
    override val allItems = listOf("sword/excalibur", "dagger/excalibur", "sword/katana")

    override fun getItemId(item: ItemStack): String? = "sword/katana".takeIf { item.type == Material.IRON_SWORD }

    override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? =
        if (itemId == "sword/katana") ItemStack(Material.IRON_SWORD, amount) else null
}

/**
 * Registry imitating QuantumRPG: an item may be referred without its module, and the same stack is returned every time.
 */
private class ModularItemRegistry : BukkitItemsRegistry {
    override val id = "quantum"
    override val knownIds = listOf("gems/ruby")

    val sharedStack = ItemStack(Material.EMERALD)
    var lastPayload: Any? = null

    override fun isSameItem(item: ItemStack, itemId: String): Boolean =
        item.type == Material.EMERALD && itemId.substringAfter('/') == "ruby"

    override fun isItemExists(itemId: String): Boolean = itemId.substringAfter('/') == "ruby"

    override fun getItemId(item: ItemStack): String? = knownIds.first().takeIf { item.type == Material.EMERALD }

    override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? {
        lastPayload = payload
        return sharedStack.takeIf { isItemExists(itemId) }
    }
}

/** Registry imitating ItemsAdder returning its own namespace in IDs. */
private class LegacyItemRegistry : BukkitItemsRegistry {
    override val id = "ia"
    override val knownIds = listOf("itemsadder:ruby_sword")

    override fun isItemExists(itemId: String): Boolean = itemId in knownIds

    override fun getItemId(item: ItemStack): String? = knownIds.first().takeIf { item.type == Material.EMERALD }

    override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? =
        if (isItemExists(itemId)) ItemStack(Material.EMERALD, amount) else null
}

/** Registry returning invalid IDs. */
private class InvalidIdItemRegistry : BukkitItemsRegistry {
    override val id = "invalid"
    override val knownIds = listOf("Ruby Sword", "emerald")

    override fun isItemExists(itemId: String): Boolean = itemId in knownIds

    override fun getItemId(item: ItemStack): String? = knownIds.first().takeIf { item.type == Material.EMERALD }

    override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? = null
}
