// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.vanilla

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.level.LevelProgress
import kotlin.test.Test

internal class MinecraftLevelProviderTest : BukkitTestBase() {

    // SUT
    private val provider = MinecraftLevelProvider()

    @Test
    fun `when get progress - should convert exp bar to points`() {
        every { player.level } returns 16
        every { player.exp } returns 0.5f
        every { player.expToLevel } returns 42

        provider.getProgress(player, "level") shouldBe
            LevelProgress.level(16).minLevel(0).exp(21.0).expToNextLevel(42.0).build()
    }

    @Test
    fun `when get progress of unknown track - should return null`() {
        provider.getProgress(player, "other").shouldBeNull()
    }

    @Test
    fun `when set level - should reset exp bar`() {
        provider.setLevel(player, "level", 10).shouldBeTrue()
        verify {
            player.level = 10
            player.exp = 0f
        }
    }

    @Test
    fun `when give and take exp - should give rounded points`() {
        provider.giveExp(player, "level", 10.6).shouldBeTrue()
        provider.takeExp(player, "level", 5.0).shouldBeTrue()
        verify {
            player.giveExp(11)
            player.giveExp(-5)
        }
    }
}
