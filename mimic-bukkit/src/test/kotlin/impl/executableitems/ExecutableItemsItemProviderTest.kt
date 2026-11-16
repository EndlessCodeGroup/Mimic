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
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

internal class ExecutableItemsItemProviderTest : BukkitTestBase() {

    private val executableItems: ExecutableItemsWrapper = mockk {
        every { itemIds } returns listOf("Free_Hammer", "Ruby Sword")
        every { hasItem(any()) } answers { firstArg<String>() in itemIds }
        every { getItemId(any()) } returns null
        every { createItem(any(), any(), any()) } answers { ItemStack(Material.STICK, secondArg()) }
    }

    // SUT
    private val provider = ExecutableItemsItemProvider(executableItems)

    @Test
    fun `when get all items - should return converted ids`() {
        provider.allItems shouldContainExactly listOf("free_hammer", "ruby_sword")
    }

    @Test
    fun `when check item exists - should find it by converted id`() {
        provider.hasItem("free_hammer") shouldBe true
        provider.hasItem("ruby_sword") shouldBe true
        provider.hasItem("unknown") shouldBe false
    }

    @Test
    fun `when create item - should create it for the player and ignore payload`() {
        val item = provider.createItem("free_hammer", amount = 2, player, payload = "payload")

        item?.amount shouldBe 2
        verify { executableItems.createItem("Free_Hammer", 2, player) }
    }

    @Test
    fun `when create unknown item - should return null`() {
        provider.createItem("unknown", amount = 1, player = null, payload = null).shouldBeNull()
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
