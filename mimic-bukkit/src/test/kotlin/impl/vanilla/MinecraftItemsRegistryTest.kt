package ru.endlesscode.mimic.impl.vanilla

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemFactory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.internal.Log
import kotlin.test.AfterTest
import kotlin.test.Test

internal class MinecraftItemsRegistryTest : BukkitTestBase() {

    private val itemMeta: ItemMeta = mockk(relaxed = true) {
        every { clone() } returns this
    }
    private val itemFactory: ItemFactory = mockk(relaxed = true) {
        every { getItemMeta(any()) } returns itemMeta
        every { isApplicable(any<ItemMeta>(), any<Material>()) } returns true
        every { asMetaFor(any(), any<Material>()) } returns itemMeta
    }

    // SUT
    private val itemsRegistry = MinecraftItemsRegistry()

    init {
        mockkStatic(Bukkit::class)
        every { Bukkit.getItemFactory() } returns itemFactory
        // Material.isItem requires Paper registries which aren't available in tests
        mockkObject(Material.STONE)
        every { Material.STONE.isItem } returns true
    }

    @AfterTest
    fun tearDown() {
        unmockkStatic(Bukkit::class)
        unmockkObject(Material.STONE)
    }

    @Test
    fun `when get item with components payload - should create item from components`() {
        val createdItem = ItemStack(Material.STONE)
        every { itemFactory.createItemStack("minecraft:stone[damage=10]") } returns createdItem

        val item = itemsRegistry.getItem("stone", "[damage=10]", amount = 100).shouldNotBeNull()

        assertSoftly {
            item shouldBeSameInstanceAs createdItem
            item.amount shouldBe 64
        }
        verify(exactly = 0) { itemFactory.getItemMeta(any()) }
    }

    @Test
    fun `when get item with invalid components payload - should return item without payload`() {
        val warnings = mutableListOf<String>()
        Log.init({ _, message, _ -> warnings += message.orEmpty() })
        every { itemFactory.createItemStack(any()) } throws IllegalArgumentException("Invalid components")

        val item = itemsRegistry.getItem("stone", "[unknown=1]", amount = 5).shouldNotBeNull()

        assertSoftly {
            item.type shouldBe Material.STONE
            item.amount shouldBe 5
            warnings shouldHaveSize 1
        }
        verify(exactly = 0) { itemFactory.getItemMeta(any()) }
    }

    @Test
    fun `when get item with HOCON payload - should apply item meta payload`() {
        val item = itemsRegistry.getItem("stone", "name = Name", amount = 5).shouldNotBeNull()

        assertSoftly {
            item.type shouldBe Material.STONE
            item.amount shouldBe 5
        }
        verify { itemMeta.displayName(text("Name")) }
        verify(exactly = 0) { itemFactory.createItemStack(any()) }
    }

    @Test
    fun `when get item with item meta payload - should apply it`() {
        val item = itemsRegistry.getItem("stone", ItemMetaPayload(name = text("Name")), amount = 5).shouldNotBeNull()

        item.type shouldBe Material.STONE
        verify { itemMeta.displayName(text("Name")) }
        verify(exactly = 0) { itemFactory.createItemStack(any()) }
    }
}
