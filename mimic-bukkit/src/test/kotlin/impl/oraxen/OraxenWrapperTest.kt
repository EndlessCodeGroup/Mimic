// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.oraxen

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.directional.DirectionalBlock
import io.th0rgal.oraxen.utils.drops.Drop
import io.th0rgal.oraxen.utils.drops.Loot
import org.bukkit.Material
import org.bukkit.configuration.MemoryConfiguration
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.internal.OnceWarnings
import kotlin.test.Test

internal class OraxenWrapperTest : BukkitTestBase() {

    @Test
    fun `when get id of directional block variant - should return id of parent block`() {
        val directional = DirectionalBlock(MemoryConfiguration().apply { set("parent_block", "ruby_log") })

        blockId("ruby_log_x", directional) shouldBe "ruby_log"
    }

    @Test
    fun `when get id of block without parent - should return its id`() {
        blockId("ruby_log", DirectionalBlock(MemoryConfiguration())) shouldBe "ruby_log"
        blockId("ruby_ore", directional = null) shouldBe "ruby_ore"
    }

    @Test
    fun `when two ids become the same - should keep the id needing no conversion and warn once`() {
        val loggedWarnings = mutableListOf<String>()
        Log.init({ _, message, _ -> loggedWarnings += message.orEmpty() })
        val warnings = OnceWarnings()
        val ids = listOf("Ruby_Sword", "ruby_sword", "Ruby Ore", "RUBY_ORE")

        repeat(2) { mimicIds(ids, warnings, "Tag").toList() shouldContainExactly listOf("ruby_sword", "ruby_ore") }

        loggedWarnings shouldContainExactly listOf(
            "[Tag] 'Ruby_Sword' is skipped: its ID 'ruby_sword' is already used by 'ruby_sword'",
            "[Tag] 'RUBY_ORE' is skipped: its ID 'ruby_ore' is already used by 'Ruby Ore'",
        )
    }

    @Test
    fun `when roll drop without tool - should treat tool requirements as satisfied`() {
        val loot: Loot = mockk {
            every { probability } returns 1.0
            every { getItem(1, null) } returns ItemStack(Material.DIAMOND)
        }
        val drop = Drop(listOf("WOODEN", "DIAMOND"), listOf(loot), true, true, "ruby_ore", "DIAMOND", listOf("PICKAXE"))

        drop.roll(tool = null).map { it.type } shouldContainExactly listOf(Material.DIAMOND)
    }
}
