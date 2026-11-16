// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.executableitems

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

internal class ExecutableItemsItemProviderTest : BukkitTestBase() {

    private val itemIds = mutableListOf("Free_Hammer", "Ruby Sword", "ruby_sword", "Ruby_Sword")

    private val executableItems: ExecutableItemsWrapper = mockk {
        every { itemIds } answers { this@ExecutableItemsItemProviderTest.itemIds }
        every { hasItem(any()) } answers { firstArg<String>() in this@ExecutableItemsItemProviderTest.itemIds }
        every { getItemId(any()) } returns null
        every { createItem(any(), any(), any()) } answers { ItemStack(Material.STICK, secondArg()) }
    }

    // SUT
    private val provider = ExecutableItemsItemProvider(executableItems)

    @Test
    fun `when get all items - should return converted ids without duplicates`() {
        provider.allItems shouldContainExactly listOf("free_hammer", "ruby_sword")
    }

    @ParameterizedTest
    @CsvSource(
        "free_hammer,   Free_Hammer",
        "ruby_sword,    ruby_sword",
    )
    fun `when create item - should use ExecutableItems id`(itemId: String, executableItemsId: String) {
        val item = provider.createItem(itemId, amount = 2, player, payload = "ignored")

        item?.amount shouldBe 2
        verify { executableItems.createItem(executableItemsId, 2, player) }
    }

    @Test
    fun `when create item and ids collide without exact id - should use the first id`() {
        itemIds.remove("ruby_sword")

        provider.createItem("ruby_sword", amount = 1, player = null, payload = null)

        verify { executableItems.createItem("Ruby Sword", 1, null) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["unknown", "free_hammer/extra"])
    fun `when create unknown item - should return null`(itemId: String) {
        provider.createItem(itemId, amount = 1, player = null, payload = null).shouldBeNull()
    }

    @ParameterizedTest
    @CsvSource(
        "free_hammer,   true",
        "ruby_sword,    true",
        "unknown,       false",
    )
    fun `when check has item - should return if item exists`(itemId: String, expected: Boolean) {
        provider.hasItem(itemId) shouldBe expected
    }

    @Test
    fun `when get item id - should return converted id`() {
        every { executableItems.getItemId(any()) } returns "Free_Hammer"

        provider.getItemId(ItemStack(Material.STICK)) shouldBe "free_hammer"
    }

    @Test
    fun `when get id of not ExecutableItems item - should return null`() {
        provider.getItemId(ItemStack(Material.STICK)).shouldBeNull()
    }
}
