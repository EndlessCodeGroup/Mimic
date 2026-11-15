// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.classes

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

internal class SingleSlotClassProviderTest : BukkitTestBase() {

    // SUT
    private val provider = TestProvider()

    @Test
    fun `when get classes - should put the class into the slot`() {
        provider.getClasses(player).mapValues { it.value.id } shouldBe mapOf("class" to "warrior")
    }

    @Test
    fun `when player has no class - should return empty slots`() {
        provider.playerClass = null
        provider.getClasses(player).shouldBeEmpty()
    }

    @Test
    fun `when set class without slot or in the own slot - should set it`() {
        provider.setClass(player, "mage", slotId = null).shouldBeTrue()
        provider.setClass(player, "paladin", slotId = "class").shouldBeTrue()
        provider.writes shouldContainExactly listOf("mage", "paladin")
    }

    @Test
    fun `when set class in another slot - should not set it`() {
        provider.setClass(player, "elf", slotId = "race").shouldBeFalse()
        provider.writes shouldBe emptyList()
    }

    private class TestProvider : SingleSlotClassProvider() {
        override val id: String = "test"
        override val allClasses: Collection<ClassProvider.ClassDefinition> = emptyList()

        var playerClass: String? = "warrior"
        val writes = mutableListOf<String>()

        override fun getClass(player: Player): ClassProvider.ClassDefinition? =
            playerClass?.let { ClassProvider.ClassDefinition(it, Component.text(it)) }

        override fun setClass(player: Player, classId: String): Boolean {
            writes += classId
            return true
        }
    }
}
