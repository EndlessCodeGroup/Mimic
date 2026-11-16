// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.nexo

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

internal class NexoItemProviderTest : BukkitTestBase() {

    private val nexo: NexoWrapper = mockk {
        every { itemIds } returns setOf("ruby", "ruby_ore")
        every { hasItem(any()) } answers { firstArg<String>() in itemIds }
        every { getItemId(any()) } returns null
        every { createItem(any(), any()) } returns null
        every { createItem("ruby", any()) } answers { ItemStack(Material.EMERALD, secondArg()) }
    }

    // SUT
    private val provider = NexoItemProvider(nexo)

    @Test
    fun `when get all items - should return Nexo ids`() {
        provider.allItems shouldContainExactly listOf("ruby", "ruby_ore")
    }

    @Test
    fun `when check item exists - should ask Nexo`() {
        provider.hasItem("ruby") shouldBe true
        provider.hasItem("unknown") shouldBe false
    }

    @Test
    fun `when create item - should ignore player and payload`() {
        val item = provider.createItem("ruby", amount = 2, player, payload = "payload")

        item?.type shouldBe Material.EMERALD
        item?.amount shouldBe 2
    }

    @Test
    fun `when create unknown item - should return null`() {
        provider.createItem("unknown", amount = 1, player = null, payload = null).shouldBeNull()
    }

    @Test
    fun `when get item id - should return Nexo id`() {
        every { nexo.getItemId(any()) } returns "ruby"

        provider.getItemId(ItemStack(Material.EMERALD)) shouldBe "ruby"
    }

    @Test
    fun `when Nexo ids are not valid - should convert them`() {
        every { nexo.itemIds } returns setOf("ruby", "Ruby Sword")
        every { nexo.getItemId(any()) } returns "Ruby Sword"
        every { nexo.createItem("Ruby Sword", any()) } answers { ItemStack(Material.DIAMOND_SWORD) }

        provider.allItems shouldContainExactly listOf("ruby", "ruby_sword")
        provider.hasItem("ruby_sword") shouldBe true
        provider.getItemId(ItemStack(Material.DIAMOND_SWORD)) shouldBe "ruby_sword"
        provider.createItem("ruby_sword", amount = 1, player = null, payload = null)?.type shouldBe Material.DIAMOND_SWORD
    }

    @Test
    fun `when Nexo ids become the same - should use the exact one`() {
        every { nexo.itemIds } returns setOf("Ruby", "ruby", "RUBY")

        provider.allItems shouldContainExactly listOf("ruby")
        provider.createItem("ruby", amount = 1, player = null, payload = null)?.type shouldBe Material.EMERALD
    }

    @Test
    fun `when Nexo ids become the same and none is exact - should use the first one`() {
        every { nexo.itemIds } returns setOf("Ruby", "RUBY")
        every { nexo.createItem("Ruby", any()) } answers { ItemStack(Material.REDSTONE) }

        provider.allItems shouldContainExactly listOf("ruby")
        provider.createItem("ruby", amount = 1, player = null, payload = null)?.type shouldBe Material.REDSTONE
    }

    @Test
    fun `when get id of item not used for its Mimic id - should return null`() {
        every { nexo.itemIds } returns setOf("Ruby", "ruby")
        every { nexo.getItemId(any()) } returns "Ruby"

        provider.getItemId(ItemStack(Material.REDSTONE)).shouldBeNull()
    }

    @Test
    fun `when get id of not Nexo item - should return null`() {
        provider.getItemId(ItemStack(Material.EMERALD)).shouldBeNull()
    }
}
