// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // LevelSystem API is deprecated but still supported

package ru.endlesscode.mimic.impl.mmocore

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.Indyuce.mmocore.api.player.PlayerData
import net.Indyuce.mmocore.experience.curve.ExperienceCurve
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

internal class MmoCoreExpLevelConverterTest : BukkitTestBase() {

    private val data: PlayerData = mockk()

    /** Like MMOCore list curves: experience from `level` to `level + 1`. */
    private val curve: ExperienceCurve = mockk {
        every { getExperience(data, any()) } answers { 100L * secondArg<Int>() }
    }

    // SUT
    private val converter = MmoCoreExpLevelConverter(
        player,
        mockk {
            every { getData(player) } returns data
            every { getExpCurve(player) } returns curve
        },
    )

    @Test
    fun `when get exp to reach level - should return exp of the step from the previous level`() {
        converter.getExpToReachLevel(1) shouldBe 0.0
        converter.getExpToReachLevel(2) shouldBe 100.0
        converter.getExpToReachLevel(3) shouldBe 200.0
    }
}
