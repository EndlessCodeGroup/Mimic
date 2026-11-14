// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.level

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.bukkit.entity.Player
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

@Suppress("DEPRECATION") // LevelSystem.exp setter
internal class LevelSystemTrackProviderTest : BukkitTestBase() {

    private val system: BukkitLevelSystem = mockk(relaxUnitFun = true) {
        every { level } returns 5
        every { exp } returns 10.0
        every { totalExpToNextLevel } returns 100.0
        every { expToNextLevel } returns 90.0
    }

    // SUT
    private val provider = LevelSystemTrackProvider(
        object : BukkitLevelSystem.Provider {
            override val id: String = "legacy"
            override fun getSystem(player: Player): BukkitLevelSystem = system
        }
    )

    @Test
    fun `when get progress - should use total requirement for the next level`() {
        provider.getProgress(player, "level") shouldBe
            LevelProgress.level(5).minLevel(0).exp(10.0).expToNextLevel(100.0).build()
    }

    @Test
    fun `when level-up is impossible - should return unknown exp to next level`() {
        every { system.totalExpToNextLevel } returns -1.0
        provider.getProgress(player, "level") shouldBe LevelProgress.level(5).minLevel(0).exp(10.0).build()
    }

    @Test
    fun `when exp is negative - should return zero exp`() {
        every { system.exp } returns -1.0
        provider.getProgress(player, "level")?.exp shouldBe 0.0
    }

    @Test
    fun `when get progress of unknown track - should return null`() {
        provider.getProgress(player, "other").shouldBeNull()
    }

    @Test
    fun `when player data doesn't exist - should return null and false`() {
        every { system.level } throws IllegalStateException("No data")
        every { system.level = any() } throws IllegalStateException("No data")

        provider.getProgress(player, "level").shouldBeNull()
        provider.setLevel(player, "level", 1).shouldBeFalse()
    }

    @Test
    fun `when operation is unsupported - should return false`() {
        every { system.giveExp(any()) } throws UnsupportedOperationException()
        provider.giveExp(player, "level", 1.0).shouldBeFalse()
    }

    @Test
    fun `when set level - should reset exp within the level`() {
        provider.setLevel(player, "level", 3).shouldBeTrue()
        verifyOrder {
            system.level = 3
            system.exp = 0.0
        }
    }

    @Test
    fun `when give and take exp - should delegate to level system`() {
        provider.giveExp(player, "level", 10.0).shouldBeTrue()
        provider.takeExp(player, "level", 5.0).shouldBeTrue()
        verify {
            system.giveExp(10.0)
            system.takeExp(5.0)
        }
    }

    @Test
    fun `when write unknown track - should return false`() {
        provider.giveExp(player, "other", 1.0).shouldBeFalse()
        verify(exactly = 0) { system.giveExp(any()) }
    }
}
