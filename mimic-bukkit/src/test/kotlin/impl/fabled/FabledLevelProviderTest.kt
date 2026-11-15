// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.fabled

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.level.LevelProgress
import studio.magemonkey.fabled.api.classes.FabledClass
import studio.magemonkey.fabled.api.enums.ExpSource
import studio.magemonkey.fabled.api.player.PlayerClass
import studio.magemonkey.fabled.api.player.PlayerData
import kotlin.test.Test

internal class FabledLevelProviderTest : BukkitTestBase() {

    private var isLoaded = true

    private val warrior: FabledClass = mockk {
        every { group } returns "class"
        every { maxLevel } returns 10
        every { receivesExp(any()) } answers { firstArg<ExpSource>() == ExpSource.COMMAND }
    }
    private val warriorProgress: PlayerClass = mockk(relaxUnitFun = true) {
        every { data } returns warrior
        every { level } returns 3
        every { exp } returns 50.0
        every { requiredExp } returns 300
    }

    private val data: PlayerData = mockk {
        every { mainClass } returns warriorProgress
        every { getClass(any()) } returns null
        every { getClass("class") } returns warriorProgress
    }

    // SUT
    private val provider = FabledLevelProvider(
        mockk {
            every { getLoadedData(player) } answers { if (isLoaded) data else null }
            every { groups } returns listOf("class", "Side Job")
        },
    )

    @Test
    fun `when get all tracks - should return groups with converted IDs`() {
        provider.allTracks.map { it.id } shouldContainExactly listOf("class", "side_job")
    }

    @Test
    fun `when get tracks - should return groups the player has a class in`() {
        provider.getTracks(player).map { it.id } shouldContainExactly listOf("class")
        provider.getPrimaryTrack(player)?.id shouldBe "class"
    }

    @Test
    fun `when data isn't loaded - should return nothing`() {
        isLoaded = false

        provider.getTracks(player).shouldBeEmpty()
        provider.getPrimaryTrack(player).shouldBeNull()
        provider.getProgress(player, "class").shouldBeNull()
        provider.setLevel(player, "class", 2).shouldBeFalse()
    }

    @Test
    fun `when player has no class in the group - should not support the track`() {
        provider.getProgress(player, "side_job").shouldBeNull()
        provider.setLevel(player, "side_job", 2).shouldBeFalse()
        provider.giveExp(player, "side_job", 10.0).shouldBeFalse()
        provider.takeExp(player, "unknown", 10.0).shouldBeFalse()
    }

    @Test
    fun `when get progress - should return level within class limits`() {
        provider.getProgress(player, "class") shouldBe
            LevelProgress.level(3).minLevel(1).maxLevel(10).exp(50.0).expToNextLevel(300.0).build()
    }

    @Test
    fun `when get progress at max level - should not have exp to next level`() {
        every { warriorProgress.level } returns 10

        provider.getProgress(player, "class") shouldBe
            LevelProgress.level(10).minLevel(1).maxLevel(10).exp(50.0).build()
    }

    @Test
    fun `when raise level - should give levels and reset exp`() {
        provider.setLevel(player, "class", 5).shouldBeTrue()

        verify {
            warriorProgress.giveLevels(2)
            warriorProgress.exp = 0.0
        }
    }

    @Test
    fun `when lower level - should take levels and reset exp`() {
        provider.setLevel(player, "class", 1).shouldBeTrue()

        verify {
            warriorProgress.loseLevels(2)
            warriorProgress.exp = 0.0
        }
    }

    @Test
    fun `when give exp - should use the command source`() {
        provider.giveExp(player, "class", 42.0).shouldBeTrue()

        verify { warriorProgress.giveExp(42.0, ExpSource.COMMAND) }
    }

    @Test
    fun `when class doesn't receive exp from commands - should not give exp`() {
        every { warrior.receivesExp(any()) } returns false

        provider.giveExp(player, "class", 42.0).shouldBeFalse()
    }

    @Test
    fun `when take exp - should allow lowering levels`() {
        provider.takeExp(player, "class", 42.0).shouldBeTrue()

        verify { warriorProgress.loseExp(42.0, false, true, true) }
    }

    @Test
    fun `when take zero exp - should not call Fabled`() {
        provider.takeExp(player, "class", 0.0).shouldBeTrue()

        verify(exactly = 0) { warriorProgress.loseExp(any(), any(), any(), any()) }
    }
}
