// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.ecoitems

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

internal class EcoItemsItemProviderTest : BukkitTestBase() {

    private val ecoItems: EcoItemsWrapper = mockk {
        every { createItem(any()) } returns null
        every { createItem("ruby") } answers { ItemStack(Material.EMERALD) }
    }

    // SUT
    private val provider = EcoItemsItemProvider(ecoItems)

    @Test
    fun `when create item - should return item with the given amount`() {
        val item = provider.createItem("ruby", amount = 3, player, payload = null)

        item?.type shouldBe Material.EMERALD
        item?.amount shouldBe 3
    }

    @Test
    fun `when create unknown item - should return null`() {
        provider.createItem("unknown", amount = 1, player = null, payload = null).shouldBeNull()
    }
}
