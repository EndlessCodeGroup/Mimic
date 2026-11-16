// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mythicmobs

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

internal class MythicMobsItemProviderTest : BukkitTestBase() {

    private val mythicMobs: MythicMobsWrapper = mockk {
        every { itemIds } returns listOf("ruby", "KingSword")
        every { hasItem(any()) } answers { firstArg<String>() in itemIds }
        every { getItemId(any()) } returns null
        every { createItem(any(), any(), any()) } returns null
        every { createItem("ruby", any(), any()) } answers { ItemStack(Material.EMERALD, secondArg()) }
        every { createItem("KingSword", any(), any()) } answers { ItemStack(Material.DIAMOND_SWORD, secondArg()) }
    }

    // SUT
    private val provider = MythicMobsItemProvider(mythicMobs)

    @Test
    fun `when get all items - should return lowercased Mythic ids`() {
        provider.allItems shouldContainExactly listOf("ruby", "kingsword")
    }

    @Test
    fun `when check item exists - should find it by converted id`() {
        provider.hasItem("ruby") shouldBe true
        provider.hasItem("kingsword") shouldBe true
        provider.hasItem("unknown") shouldBe false
    }

    @Test
    fun `when create item - should create it for the player and ignore payload`() {
        val item = provider.createItem("kingsword", amount = 2, player, payload = "payload")

        item?.type shouldBe Material.DIAMOND_SWORD
        item?.amount shouldBe 2
        verify { mythicMobs.createItem("KingSword", 2, player) }
    }

    @Test
    fun `when create unknown item - should return null`() {
        provider.createItem("unknown", amount = 1, player = null, payload = null).shouldBeNull()
    }

    @Test
    fun `when get item id - should return converted Mythic id`() {
        every { mythicMobs.getItemId(any()) } returns "King Sword"

        provider.getItemId(ItemStack(Material.DIAMOND_SWORD)) shouldBe "king_sword"
    }

    @Test
    fun `when get id of not Mythic item - should return null`() {
        provider.getItemId(ItemStack(Material.EMERALD)).shouldBeNull()
    }

    @Test
    fun `when Mythic ids become the same - should use the exact one`() {
        every { mythicMobs.itemIds } returns listOf("Ruby", "ruby", "RUBY")

        provider.allItems shouldContainExactly listOf("ruby")
        provider.createItem("ruby", amount = 1, player = null, payload = null)?.type shouldBe Material.EMERALD
        verify { mythicMobs.createItem("ruby", 1, null) }
    }

    @Test
    fun `when Mythic ids become the same and none is exact - should use the first one`() {
        every { mythicMobs.itemIds } returns listOf("Ruby", "RUBY")
        every { mythicMobs.createItem("Ruby", any(), any()) } answers { ItemStack(Material.REDSTONE) }

        provider.allItems shouldContainExactly listOf("ruby")
        provider.createItem("ruby", amount = 1, player = null, payload = null)?.type shouldBe Material.REDSTONE
    }
}
