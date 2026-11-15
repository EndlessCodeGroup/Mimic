// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.vanilla

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.resources.ResourceState
import kotlin.test.Test

internal class MinecraftResourceProviderTest : BukkitTestBase() {

    // SUT
    private val provider = MinecraftResourceProvider()

    @Test
    fun `when get resources - should return health, food and air without primary resource`() {
        provider.allResources.map { it.id } shouldContainExactly listOf("health", "food", "air")
        provider.getPrimaryResource(player).shouldBeNull()
    }

    @Test
    fun `when get health - should return health and max health`() {
        every { player.health } returns 7.5
        @Suppress("DEPRECATION")
        every { player.maxHealth } returns 24.0

        provider.getState(player, "health") shouldBe ResourceState.of(7.5, 24.0)
    }

    @Test
    fun `when set health - should set it without rounding`() {
        every { player.isDead } returns false

        provider.setAmount(player, "health", 0.5).shouldBeTrue()

        verify { player.health = 0.5 }
    }

    @Test
    fun `when set health of dead player - should not change it`() {
        every { player.isDead } returns true

        provider.setAmount(player, "health", 5.0).shouldBeFalse()

        verify(exactly = 0) { player.health = any() }
    }

    @Test
    fun `when get food - should return food level`() {
        every { player.foodLevel } returns 14

        provider.getState(player, "food") shouldBe ResourceState.of(14.0, 20.0)
    }

    @Test
    fun `when get air - should return remaining air in ticks`() {
        every { player.remainingAir } returns 120
        every { player.maximumAir } returns 300

        provider.getState(player, "air") shouldBe ResourceState.of(120.0, 300.0)
    }

    @Test
    fun `when player is drowning - should return no air`() {
        every { player.remainingAir } returns -15
        every { player.maximumAir } returns 300

        provider.getState(player, "air") shouldBe ResourceState.of(0.0, 300.0)
    }

    @Test
    fun `when set amount - should round it down`() {
        provider.setAmount(player, "food", 7.9).shouldBeTrue()
        provider.setAmount(player, "air", 59.5).shouldBeTrue()

        verify {
            player.foodLevel = 7
            player.remainingAir = 59
        }
    }

    @Test
    fun `when resource is unknown - should return nothing`() {
        provider.getState(player, "mana").shouldBeNull()
        provider.setAmount(player, "mana", 1.0).shouldBeFalse()
    }
}
