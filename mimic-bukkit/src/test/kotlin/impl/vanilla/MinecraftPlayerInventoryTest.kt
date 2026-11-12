// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.vanilla

import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

class MinecraftPlayerInventoryTest : BukkitTestBase() {

    private val playerInventory: PlayerInventory = mockk()

    // SUT
    private val inventory = MinecraftPlayerInventory(player)

    init {
        every { player.inventory } returns playerInventory
    }

    @Test
    fun `when get stored items and another slot has equal stack - should exclude only held slot`() {
        val heldItem = item()
        val equalItem = item()
        every { equalItem == heldItem } returns true
        every { playerInventory.storageContents } returns arrayOf(equalItem, heldItem, null)
        every { playerInventory.heldItemSlot } returns 1

        inventory.storedItems.shouldContainExactly(equalItem)
    }

    private fun item(): ItemStack = mockk {
        every { isEmpty } returns false
    }
}
