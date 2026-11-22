// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.itemsadder

import dev.lone.itemsadder.api.CustomStack
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

internal class ItemsAdderItemProviderTest : BukkitTestBase() {

    private val itemsAdder: ItemsAdderWrapper = mockk {
        every { itemIds } returns setOf("itemsadder:ruby_sword", "my_items:ruby_sword", "My_Items:Ruby_Axe", "itemsadder:swords/ruby")
        every { hasItem(any()) } answers { firstArg<String>() in itemIds }
        every { getCustomStack(any<String>()) } returns null
        every { getCustomStack("itemsadder:ruby_sword") } answers { mockCustomStack(Material.DIAMOND_SWORD) }
        every { getCustomStack("my_items:ruby_sword") } answers { mockCustomStack(Material.IRON_SWORD) }
        every { getCustomStack("My_Items:Ruby_Axe") } answers { mockCustomStack(Material.IRON_AXE) }
        every { getCustomStack(any<ItemStack>()) } returns null
    }

    // SUT
    private val provider = ItemsAdderItemProvider(itemsAdder)

    @Test
    fun `when get all items - should drop default namespace and keep others as the first segment`() {
        provider.allItems shouldContainExactly listOf("ruby_sword", "my_items/ruby_sword", "my_items/ruby_axe", "itemsadder/swords/ruby")
    }

    @ParameterizedTest
    @CsvSource(
        "ruby_sword,          true",
        "my_items/ruby_sword, true",
        "my_items/ruby_axe,   true",
        "my_items/unknown,    false",
        "unknown,             false",
    )
    fun `when check item exists - should look for ItemsAdder id`(itemId: String, expected: Boolean) {
        provider.hasItem(itemId) shouldBe expected
    }

    @ParameterizedTest
    @CsvSource(
        "ruby_sword,          DIAMOND_SWORD",
        "my_items/ruby_sword, IRON_SWORD",
        "my_items/ruby_axe,   IRON_AXE",
    )
    fun `when create item - should return the item of the given namespace`(itemId: String, expected: Material) {
        val item = provider.createItem(itemId, amount = 2, player, payload = "ignored")

        item?.type shouldBe expected
        item?.amount shouldBe 2
    }

    @ParameterizedTest
    @ValueSource(strings = ["unknown", "my_items/unknown", "unknown/ruby_sword"])
    fun `when create unknown item - should return null`(itemId: String) {
        provider.createItem(itemId, amount = 1, player = null, payload = null).shouldBeNull()
    }

    @ParameterizedTest
    @CsvSource(
        "itemsadder:ruby_sword,  ruby_sword",
        "my_items:ruby_sword,    my_items/ruby_sword",
        "itemsadder:swords/ruby, itemsadder/swords/ruby",
        "My_Items:Ruby_Axe,      my_items/ruby_axe",
    )
    fun `when get item id - should convert ItemsAdder id`(itemsAdderId: String, expected: String) {
        val item = mockk<ItemStack>()
        every { itemsAdder.getCustomStack(item) } returns mockk { every { namespacedID } returns itemsAdderId }

        provider.getItemId(item) shouldBe expected
    }

    @Test
    fun `when get id of not ItemsAdder item - should return null`() {
        provider.getItemId(ItemStack(Material.IRON_SWORD)).shouldBeNull()
    }

    private fun mockCustomStack(type: Material): CustomStack = mockk { every { itemStack } returns ItemStack(type) }
}
