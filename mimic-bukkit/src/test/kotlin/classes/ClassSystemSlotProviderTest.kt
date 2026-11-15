// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // ClassSystem API is deprecated but still supported

package ru.endlesscode.mimic.classes

import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

internal class ClassSystemSlotProviderTest : BukkitTestBase() {

    private val system: BukkitClassSystem = mockk {
        every { primaryClass } returns "Arcane Mage"
    }

    // SUT
    private val provider = ClassSystemSlotProvider(
        object : BukkitClassSystem.Provider {
            override val id: String = "legacy"
            override fun getSystem(player: Player): BukkitClassSystem = system
        }
    )

    @Test
    fun `when get classes - should convert the primary class name to ID`() {
        val playerClass = provider.getClasses(player).getValue("class")

        playerClass.id shouldBe "arcane_mage"
        playerClass.name shouldBe Component.text("Arcane Mage")
    }

    @Test
    fun `when class name has color codes - should strip them from ID`() {
        every { system.primaryClass } returns "§6Arcane Mage"

        provider.getClasses(player).getValue("class").id shouldBe "arcane_mage"
    }

    @Test
    fun `when class name has only color codes - should return empty slots`() {
        every { system.primaryClass } returns "§6"
        provider.getClasses(player).shouldBeEmpty()
    }

    @Test
    fun `when player has no class - should return empty slots`() {
        every { system.primaryClass } returns null
        provider.getClasses(player).shouldBeEmpty()
    }

    @Test
    fun `when player data does not exist - should return empty slots`() {
        every { system.primaryClass } throws IllegalStateException("No data")
        provider.getClasses(player).shouldBeEmpty()
    }

    @Test
    fun `when get primary slot - should return the single slot`() {
        provider.getPrimarySlot(player).id shouldBe "class"
        provider.allSlots.map { it.id } shouldBe listOf("class")
    }
}
