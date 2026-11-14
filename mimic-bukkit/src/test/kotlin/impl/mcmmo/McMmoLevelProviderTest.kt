// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mcmmo

import com.gmail.nossr50.datatypes.player.McMMOPlayer
import com.gmail.nossr50.datatypes.skills.PrimarySkillType
import com.gmail.nossr50.util.skills.SkillTools
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.level.LevelProgress
import kotlin.test.Test

internal class McMmoLevelProviderTest : BukkitTestBase() {

    private val mining = PrimarySkillType.MINING
    private var isLoaded = true

    /** Levels and experience stored like in mcMMO profile, requirement is `100 * level + 100`. */
    private val levels = mutableMapOf<PrimarySkillType, Int>().withDefault { 0 }
    private val exp = mutableMapOf<PrimarySkillType, Float>().withDefault { 0f }

    private val mcMmoPlayer: McMMOPlayer = mockk(relaxUnitFun = true) {
        every { getSkillLevel(any()) } answers { levels.getValue(firstArg()) }
        every { getSkillXpLevelRaw(any()) } answers { exp.getValue(firstArg()) }
        every { getXpToLevel(any()) } answers { 100 * levels.getValue(firstArg()) + 100 }
        every { modifySkill(any(), any()) } answers {
            levels[firstArg()] = secondArg()
            exp[firstArg()] = 0f
        }
        every { setSkillXpLevel(any(), any()) } answers { exp[firstArg()] = secondArg() }
    }

    private val skillTools: SkillTools = mockk {
        every { getLocalizedSkillName(any()) } answers { firstArg<PrimarySkillType>().name }
        every { getLevelCap(any()) } returns Int.MAX_VALUE
    }
    private var powerLevelCap = Int.MAX_VALUE

    // SUT
    private val provider = McMmoLevelProvider(
        getMcMmoPlayer = { if (isLoaded) mcMmoPlayer else null },
        getSkillTools = { skillTools },
        getPowerLevelCap = { powerLevelCap },
    )

    @Test
    fun `when get all tracks - should return skills and power`() {
        val trackIds = provider.allTracks.map { it.id }

        trackIds.size shouldBe PrimarySkillType.entries.size + 1
        trackIds shouldContain "mining"
        trackIds shouldContain "salvage"
        trackIds shouldContain "power"
    }

    @Test
    fun `when get tracks of not loaded player - should return empty list`() {
        isLoaded = false

        provider.getTracks(player).shouldBeEmpty()
    }

    @Test
    fun `when get primary track - should return power`() {
        provider.getPrimaryTrack(player).id shouldBe "power"
    }

    @Test
    fun `when get skill progress - should return level and exp`() {
        levels[mining] = 2
        exp[mining] = 150.5f

        provider.getProgress(player, "mining") shouldBe
            LevelProgress.level(2).minLevel(0).exp(150.5).expToNextLevel(300.0).build()
    }

    @Test
    fun `when get progress with level cap - should have max level`() {
        every { skillTools.getLevelCap(mining) } returns 100
        levels[mining] = 2

        provider.getProgress(player, "mining") shouldBe
            LevelProgress.level(2).minLevel(0).maxLevel(100).exp(0.0).expToNextLevel(300.0).build()
    }

    @Test
    fun `when get progress at level cap - should not have exp to next level`() {
        every { skillTools.getLevelCap(mining) } returns 100
        levels[mining] = 100

        provider.getProgress(player, "mining") shouldBe
            LevelProgress.level(100).minLevel(0).maxLevel(100).exp(0.0).build()
    }

    @Test
    fun `when get child skill progress - should return level without exp`() {
        levels[PrimarySkillType.SALVAGE] = 5

        provider.getProgress(player, "salvage") shouldBe LevelProgress.level(5).minLevel(0).build()
    }

    @Test
    fun `when get power progress - should return power level with cap`() {
        every { mcMmoPlayer.powerLevel } returns 7
        powerLevelCap = 1000

        provider.getProgress(player, "power") shouldBe LevelProgress.level(7).minLevel(0).maxLevel(1000).build()
    }

    @Test
    fun `when get progress of not loaded player or unknown track - should return null`() {
        provider.getProgress(player, "unknown").shouldBeNull()
        isLoaded = false
        provider.getProgress(player, "mining").shouldBeNull()
    }

    @Test
    fun `when raise level - should add levels to fire level-up event`() {
        levels[mining] = 2

        provider.setLevel(player, "mining", 5).shouldBeTrue()

        verify { mcMmoPlayer.addLevels(mining, 3) }
    }

    @Test
    fun `when lower level - should modify skill`() {
        levels[mining] = 5

        provider.setLevel(player, "mining", 2).shouldBeTrue()

        verify { mcMmoPlayer.modifySkill(mining, 2) }
    }

    @Test
    fun `when change power or child skill - should not be supported`() {
        for (trackId in listOf("power", "salvage")) {
            provider.setLevel(player, trackId, 10).shouldBeFalse()
            provider.giveExp(player, trackId, 10.0).shouldBeFalse()
            provider.takeExp(player, trackId, 10.0).shouldBeFalse()
        }
    }

    @Test
    fun `when give exp - should add exact exp`() {
        provider.giveExp(player, "mining", 42.5).shouldBeTrue()

        verify { mcMmoPlayer.addXp(mining, 42.5f) }
    }

    @Test
    fun `when take exp within level - should keep level`() {
        levels[mining] = 2
        exp[mining] = 150f

        provider.takeExp(player, "mining", 50.0).shouldBeTrue()

        levels[mining] shouldBe 2
        exp[mining] shouldBe 100f
    }

    @Test
    fun `when take exp above level exp - should lower level`() {
        levels[mining] = 2
        exp[mining] = 50f

        // 50 exp on level 2, then 200 exp of the step from level 1 to 2
        provider.takeExp(player, "mining", 100.0).shouldBeTrue()

        levels[mining] shouldBe 1
        exp[mining] shouldBe 150f
    }

    @Test
    fun `when take more exp than total - should stop at level 0`() {
        levels[mining] = 2
        exp[mining] = 50f

        provider.takeExp(player, "mining", 1000.0).shouldBeTrue()

        verifyOrder {
            mcMmoPlayer.modifySkill(mining, 1)
            mcMmoPlayer.modifySkill(mining, 0)
            mcMmoPlayer.setSkillXpLevel(mining, 0f)
        }
    }

    @Test
    fun `when change level of not loaded player - should return false`() {
        isLoaded = false

        provider.setLevel(player, "mining", 10).shouldBeFalse()
        provider.giveExp(player, "mining", 10.0).shouldBeFalse()
        provider.takeExp(player, "mining", 10.0).shouldBeFalse()
    }
}
