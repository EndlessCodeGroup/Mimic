// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.nexo

import com.nexomc.nexo.utils.drops.Drop
import com.nexomc.nexo.utils.drops.Loot
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.inventory.ItemStack
import kotlin.test.Test

internal class NexoWrapperTest {

    private val tool: ItemStack = mockk()
    private val lootItem: ItemStack = mockk { every { amount } returns 1 }
    private val silkTouchItem: ItemStack = mockk()
    private val loot: Loot = mockk {
        every { probability } returns 1.0
        every { getItem(any()) } returns lootItem
    }
    private val drop: Drop = mockk {
        every { loots } returns mutableListOf(loot)
        every { sourceID } returns "ruby_ore"
        every { canDrop(any()) } returns true
        every { isSilkTouchDrop(any()) } returns false
        every { fortuneMultiplier(any()) } returns 3
    }

    @Test
    fun `when roll drops without tool - should ignore tool requirements and enchantments`() {
        every { drop.canDrop(any()) } returns false

        drop.rollDrops(tool = null) { silkTouchItem } shouldContainExactly listOf(lootItem)
        verify { loot.getItem(1) }
    }

    @Test
    fun `when roll drops with not suitable tool - should drop nothing`() {
        every { drop.canDrop(tool) } returns false

        drop.rollDrops(tool) { silkTouchItem }.shouldBeEmpty()
    }

    @Test
    fun `when roll drops with fortune tool - should multiply loot`() {
        drop.rollDrops(tool) { silkTouchItem } shouldContainExactly listOf(lootItem)
        verify { loot.getItem(3) }
    }

    @Test
    fun `when roll drops with silk touch tool - should drop the block item`() {
        every { drop.isSilkTouchDrop(tool) } returns true

        drop.rollDrops(tool) { silkTouchItem } shouldContainExactly listOf(silkTouchItem)
    }
}
