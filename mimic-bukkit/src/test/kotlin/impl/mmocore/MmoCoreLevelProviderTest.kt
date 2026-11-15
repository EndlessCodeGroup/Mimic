// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mmocore

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import net.Indyuce.mmocore.api.event.PlayerLevelChangeEvent.Reason
import net.Indyuce.mmocore.api.player.PlayerData
import net.Indyuce.mmocore.api.player.profess.PlayerClass
import net.Indyuce.mmocore.experience.EXPSource
import net.Indyuce.mmocore.experience.PlayerProfessions
import net.Indyuce.mmocore.experience.Profession
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.level.LevelProgress
import kotlin.test.Test

internal class MmoCoreLevelProviderTest : BukkitTestBase() {

    private var mainLevel = 1
    private var mainExp = 0.0
    private var isReady = true

    private val playerClass: PlayerClass = mockk(relaxUnitFun = true) {
        every { maxLevel } returns 0
    }
    private val mining: Profession = mockk(relaxUnitFun = true) {
        every { id } returns "mining"
        every { name } returns "&aMining"
        every { maxLevel } returns 20
    }
    private val professions: PlayerProfessions = mockk(relaxUnitFun = true)

    /** Mirrors MMOCore with a formula curve: the next level requires `100 * main level` experience. */
    private val data: PlayerData = mockk(relaxUnitFun = true) {
        every { profess } returns playerClass
        every { collectionSkills } returns professions
        every { level } answers { mainLevel }
        every { experience } answers { mainExp }
        every { levelUpExperience } answers { 100L * mainLevel }
        every { setLevel(any(), any()) } answers { mainLevel = firstArg() }
        every { experience = any() } answers { mainExp = firstArg() }
    }

    // SUT
    private val provider = MmoCoreLevelProvider(
        mockk {
            every { getLoadedData(player) } answers { if (isReady) data else null }
            every { professions } returns listOf(mining)
        },
    )

    @Test
    fun `when get all tracks - should return level and professions`() {
        provider.allTracks.map { it.id } shouldContainExactly listOf("level", "profession/mining")
    }

    @Test
    fun `when data isn't ready - should return nothing`() {
        isReady = false

        provider.getTracks(player).shouldBeEmpty()
        provider.getProgress(player, "level").shouldBeNull()
        provider.setLevel(player, "level", 2).shouldBeFalse()
    }

    @Test
    fun `when get main progress - should return unlimited level`() {
        mainLevel = 3
        mainExp = 50.0

        provider.getProgress(player, "level") shouldBe
            LevelProgress.level(3).minLevel(1).exp(50.0).expToNextLevel(300.0).build()
    }

    @Test
    fun `when get main progress below max level - should return max level and exp to next level`() {
        every { playerClass.maxLevel } returns 10
        mainLevel = 3

        provider.getProgress(player, "level") shouldBe
            LevelProgress.level(3).minLevel(1).maxLevel(10).exp(0.0).expToNextLevel(300.0).build()
    }

    @Test
    fun `when track is unknown - should not support it`() {
        provider.getProgress(player, "profession/unknown").shouldBeNull()
        provider.setLevel(player, "profession/unknown", 2).shouldBeFalse()
        provider.giveExp(player, "profession/unknown", 10.0).shouldBeFalse()
        provider.takeExp(player, "profession/unknown", 10.0).shouldBeFalse()
    }

    @Test
    fun `when get profession progress at max level - should not have exp to next level`() {
        every { professions.getLevel(mining) } returns 20
        every { professions.getExperience(mining) } returns 0.0

        provider.getProgress(player, "profession/mining") shouldBe
            LevelProgress.level(20).minLevel(1).maxLevel(20).exp(0.0).build()
    }

    @Test
    fun `when raise main level - should claim exp tables and run level-up triggers`() {
        mainLevel = 2

        provider.setLevel(player, "level", 4).shouldBeTrue()

        verifyOrder {
            playerClass.updateAdvancement(data, 3)
            playerClass.updateAdvancement(data, 4)
            data.setLevel(4, Reason.LEVEL_UP)
        }
        mainExp shouldBe 0.0
    }

    @Test
    fun `when lower profession level - should set level and reset exp`() {
        every { professions.getLevel(mining) } returns 5

        provider.setLevel(player, "profession/mining", 2).shouldBeTrue()

        verify(exactly = 0) { mining.updateAdvancement(any(), any()) }
        verify {
            professions.setLevel(mining, 2, Reason.COMMAND)
            professions.setExperience(mining, 0.0)
        }
    }

    @Test
    fun `when give exp - should skip party sharing`() {
        provider.giveExp(player, "level", 42.0).shouldBeTrue()
        provider.giveExp(player, "profession/mining", 42.0).shouldBeTrue()

        verify {
            data.giveExperience(42.0, EXPSource.OTHER, null, false)
            professions.giveExperience(mining, 42.0, EXPSource.OTHER, null, false)
        }
    }

    @Test
    fun `when take main exp across levels - should read requirement after lowering`() {
        mainLevel = 3
        mainExp = 50.0

        // 50 exp on level 3, then 200 exp of the step from level 2 to 3
        provider.takeExp(player, "level", 100.0).shouldBeTrue()

        mainLevel shouldBe 2
        mainExp shouldBe 150.0
    }

    @Test
    fun `when take main exp across a free level - should continue lowering`() {
        every { data.levelUpExperience } answers { if (mainLevel == 2) 0 else 100L * mainLevel }
        mainLevel = 3
        mainExp = 50.0

        // 50 exp on level 3, nothing on the free level 2, then 100 exp of the step from level 1 to 2
        provider.takeExp(player, "level", 100.0).shouldBeTrue()

        mainLevel shouldBe 1
        mainExp shouldBe 50.0
    }

    @Test
    fun `when take more main exp than total - should stop at level 1`() {
        mainLevel = 3
        mainExp = 50.0

        provider.takeExp(player, "level", 1000.0).shouldBeTrue()

        mainLevel shouldBe 1
        mainExp shouldBe 0.0
    }
}
