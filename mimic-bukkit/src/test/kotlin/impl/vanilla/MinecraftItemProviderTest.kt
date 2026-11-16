// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.vanilla

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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

internal class MinecraftItemProviderTest : BukkitTestBase() {

    private val itemMeta: ItemMeta = mockk(relaxed = true) {
        every { clone() } returns this
    }
    private val itemFactory: ItemFactory = mockk(relaxed = true) {
        every { getItemMeta(any()) } returns itemMeta
        every { isApplicable(any<ItemMeta>(), any<Material>()) } returns true
        every { asMetaFor(any(), any<Material>()) } returns itemMeta
    }

    // SUT
    private val provider = MinecraftItemProvider()

    init {
        mockkStatic(Bukkit::class)
        every { Bukkit.getItemFactory() } returns itemFactory
        // Material.isItem and Material.isAir require Paper registries which aren't available in tests
        mockkObject(Material.STONE, Material.AIR)
        every { Material.STONE.isItem } returns true
        every { Material.STONE.isAir } returns false
        every { Material.AIR.isAir } returns true
    }

    @AfterTest
    fun tearDown() {
        unmockkStatic(Bukkit::class)
        unmockkObject(Material.STONE, Material.AIR)
    }

    @Test
    fun `when create item - should return new stack with amount`() {
        val item = provider.createItem("stone", amount = 5, player = null, payload = null).shouldNotBeNull()

        assertSoftly {
            item.type shouldBe Material.STONE
            item.amount shouldBe 5
        }
    }

    @Test
    fun `when create unknown item - should return null`() {
        assertSoftly {
            provider.createItem("unknown", amount = 1, player = null, payload = null).shouldBeNull()
            provider.createItem("air", amount = 1, player = null, payload = null).shouldBeNull()
            provider.createItem("legacy_stone", amount = 1, player = null, payload = null).shouldBeNull()
        }
    }

    @Test
    fun `when get item id - should return material name`() {
        assertSoftly {
            provider.getItemId(ItemStack(Material.STONE)) shouldBe "stone"
            provider.getItemId(ItemStack(Material.AIR)).shouldBeNull()
        }
    }

    @Test
    fun `when create item with components payload - should create item from components`() {
        val createdItem = ItemStack(Material.STONE)
        every { itemFactory.createItemStack("minecraft:stone[damage=10]") } returns createdItem

        val item = provider.createItem("stone", amount = 5, player = null, payload = "[damage=10]").shouldNotBeNull()

        assertSoftly {
            item shouldBeSameInstanceAs createdItem
            item.amount shouldBe 5
        }
        verify(exactly = 0) { itemFactory.getItemMeta(any()) }
    }

    @Test
    fun `when create item with invalid components payload - should return null`() {
        every { itemFactory.createItemStack(any()) } throws IllegalArgumentException("Invalid components")

        provider.createItem("stone", amount = 5, player = null, payload = "[unknown=1]").shouldBeNull()
    }

    @Test
    fun `when create item with deprecated HOCON payload - should apply it and warn once`() {
        val warnings = mutableListOf<String>()
        Log.init({ _, message, _ -> warnings += message.orEmpty() })

        provider.createItem("stone", amount = 5, player = null, payload = "name = Name").shouldNotBeNull()
        val item = provider.createItem("stone", amount = 5, player = null, payload = "name = Name").shouldNotBeNull()

        assertSoftly {
            item.type shouldBe Material.STONE
            item.amount shouldBe 5
            warnings.single() shouldContain "HOCON format is deprecated"
        }
        verify { itemMeta.displayName(text("Name")) }
        verify(exactly = 0) { itemFactory.createItemStack(any()) }
    }

    @Test
    fun `when create item with invalid HOCON payload - should return null`() {
        provider.createItem("stone", amount = 5, player = null, payload = "name = {").shouldBeNull()
    }
}
