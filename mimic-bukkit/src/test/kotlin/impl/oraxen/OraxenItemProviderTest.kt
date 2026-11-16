// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.oraxen

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

internal class OraxenItemProviderTest : BukkitTestBase() {

    private val items = mapOf(
        "ruby_sword" to Material.DIAMOND_SWORD,
        "Ruby Ore" to Material.PAPER,
    )

    private val oraxen: OraxenWrapper = mockk {
        every { itemIds } returns items.keys
        every { hasItem(any()) } answers { firstArg() in items }
        every { getItemId(any()) } returns null
        every { createItem(any()) } answers { ItemStack(items.getValue(firstArg())) }
    }

    // SUT
    private val provider = OraxenItemProvider(oraxen)

    @Test
    fun `when get all items - should return converted ids`() {
        provider.allItems.toList() shouldContainExactly listOf("ruby_sword", "ruby_ore")
    }

    @ParameterizedTest
    @CsvSource(
        "ruby_sword, true",
        "ruby_ore,   true",
        "unknown,    false",
    )
    fun `when check item exists - should find it by converted id`(itemId: String, expected: Boolean) {
        provider.hasItem(itemId) shouldBe expected
    }

    @ParameterizedTest
    @CsvSource(
        "ruby_sword, DIAMOND_SWORD",
        "ruby_ore,   PAPER",
    )
    fun `when create item - should create it by original id`(itemId: String, expected: Material) {
        val item = provider.createItem(itemId, amount = 2, player, payload = "ignored")

        item?.type shouldBe expected
        item?.amount shouldBe 2
    }

    @Test
    fun `when create unknown item - should return null`() {
        provider.createItem("unknown", amount = 1, player = null, payload = null).shouldBeNull()
    }

    @Test
    fun `when get item id - should return converted id`() {
        every { oraxen.getItemId(any()) } returns "Ruby Ore"

        provider.getItemId(ItemStack(Material.PAPER)) shouldBe "ruby_ore"
    }

    @Test
    fun `when get id of not Oraxen item - should return null`() {
        provider.getItemId(ItemStack(Material.PAPER)).shouldBeNull()
    }
}
