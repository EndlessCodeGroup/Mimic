// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.valhallammo

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent.ExperienceGainReason
import me.athlaeos.valhallammo.playerstats.profiles.Profile
import me.athlaeos.valhallammo.skills.skills.Skill
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.level.LevelProgress
import kotlin.test.Test

internal class ValhallaMmoLevelProviderTest : BukkitTestBase() {

    private var isLoaded = true
    private var isChangeCancelled = false
    private var maxAllowedLevel = Short.MAX_VALUE.toInt()

    /** Level and experience stored like in ValhallaMMO profile, requirement to reach a level is `100 * level + 100`. */
    private var level = 0
    private var exp = 0.0

    private val profile: Profile = mockk {
        every { level } answers { this@ValhallaMmoLevelProviderTest.level }
        every { exp } answers { this@ValhallaMmoLevelProviderTest.exp }
    }

    private val power = mockSkill("POWER")
    private val mining = mockSkill("MINING")
    private val lightArmor = mockSkill("LIGHT_ARMOR")
    private val notLevelable = mockSkill("NOT_LEVELABLE", isLevelable = false)

    private val valhallaMmo: ValhallaMmoWrapper = mockk {
        every { skills } returns listOf(power, mining, lightArmor, notLevelable)
        every { isLoaded(player) } answers { isLoaded }
        every { getProfile(player, any()) } returns profile
        every { getMaxAllowedLevel(player, any()) } answers { maxAllowedLevel }
    }

    // SUT
    private val provider = ValhallaMmoLevelProvider(valhallaMmo)

    @Test
    fun `when get all tracks - should return levelable skills`() {
        provider.allTracks.map { it.id } shouldContainExactly listOf("power", "mining", "light_armor")
    }

    @Test
    fun `when get tracks of not loaded player - should return empty list`() {
        isLoaded = false

        provider.getTracks(player).shouldBeEmpty()
    }

    @Test
    fun `when get primary track - should return power`() {
        provider.getPrimaryTrack(player)?.id shouldBe "power"
    }

    @Test
    fun `when get progress - should return level and exp`() {
        level = 2
        exp = 150.5

        provider.getProgress(player, "mining") shouldBe
            LevelProgress.level(2).minLevel(0).maxLevel(MAX_LEVEL).exp(150.5).expToNextLevel(400.0).build()
    }

    @Test
    fun `when get progress at max level - should not have exp to next level`() {
        level = MAX_LEVEL
        exp = 150.5

        provider.getProgress(player, "mining") shouldBe
            LevelProgress.level(MAX_LEVEL).minLevel(0).maxLevel(MAX_LEVEL).exp(150.5).build()
    }

    @Test
    fun `when get progress with max allowed level - should use it as max level`() {
        level = 5
        maxAllowedLevel = 5

        provider.getProgress(player, "mining") shouldBe
            LevelProgress.level(5).minLevel(0).maxLevel(5).exp(0.0).build()
    }

    @Test
    fun `when get progress of not loaded player or unknown track - should return null`() {
        provider.getProgress(player, "unknown").shouldBeNull()
        provider.getProgress(player, "not_levelable").shouldBeNull()
        isLoaded = false
        provider.getProgress(player, "mining").shouldBeNull()
    }

    @Test
    fun `when raise level - should reach level with zero exp`() {
        level = 2
        exp = 150.5

        provider.setLevel(player, "mining", 5).shouldBeTrue()

        level shouldBe 5
        exp shouldBe 0.0
    }

    @Test
    fun `when raise level to max level - should reach level with zero exp`() {
        level = 2
        exp = 150.5

        provider.setLevel(player, "mining", MAX_LEVEL).shouldBeTrue()

        level shouldBe MAX_LEVEL
        exp shouldBe 0.0
    }

    @Test
    fun `when lower level - should reach level with zero exp`() {
        level = 5
        exp = 150.5

        provider.setLevel(player, "mining", 2).shouldBeTrue()

        level shouldBe 2
        exp shouldBe 0.0
    }

    @Test
    fun `when lower level by one without exp - should reach level with zero exp`() {
        level = 5

        provider.setLevel(player, "mining", 4).shouldBeTrue()

        level shouldBe 4
        exp shouldBe 0.0
    }

    @Test
    fun `when lower level to zero - should reach level with zero exp`() {
        level = 5
        exp = 150.5

        provider.setLevel(player, "mining", 0).shouldBeTrue()

        level shouldBe 0
        exp shouldBe 0.0
    }

    @Test
    fun `when set current level - should reset exp only`() {
        level = 5
        exp = 150.5

        provider.setLevel(player, "mining", 5).shouldBeTrue()

        level shouldBe 5
        exp shouldBe 0.0
        verify(exactly = 1) { mining.addEXP(player, -150.5, true, ExperienceGainReason.PLUGIN) }
    }

    @Test
    fun `when set level is cancelled - should keep exp`() {
        level = 2
        exp = 150.5
        isChangeCancelled = true

        provider.setLevel(player, "mining", 5).shouldBeTrue()

        verify(exactly = 1) { mining.addEXP(any(), any(), any(), any()) }
    }

    @Test
    fun `when give exp - should add exact exp`() {
        provider.giveExp(player, "mining", 42.0).shouldBeTrue()

        verify { mining.addEXP(player, 42.0, false, ExperienceGainReason.PLUGIN) }
    }

    @Test
    fun `when take exp - should add negative exp`() {
        provider.takeExp(player, "mining", 42.0).shouldBeTrue()

        verify { mining.addEXP(player, -42.0, false, ExperienceGainReason.PLUGIN) }
    }

    @Test
    fun `when change power - should be supported`() {
        provider.giveExp(player, "power", 42.0).shouldBeTrue()

        verify { power.addEXP(player, 42.0, false, ExperienceGainReason.PLUGIN) }
    }

    @Test
    fun `when change level of not loaded player - should return false`() {
        isLoaded = false

        shouldNotChangeLevel()
    }

    @Test
    fun `when change level without skill permission - should return false`() {
        every { mining.hasPermissionAccess(player) } returns false

        shouldNotChangeLevel()
    }

    @Test
    fun `when change level with reached daily limit - should return false`() {
        every { mining.hasReachedLimit(player) } returns true

        shouldNotChangeLevel()
    }

    @Test
    fun `when change level at max allowed level - should return false`() {
        level = 5
        maxAllowedLevel = 5

        shouldNotChangeLevel()
    }

    private fun shouldNotChangeLevel() {
        provider.setLevel(player, "mining", 10).shouldBeFalse()
        provider.giveExp(player, "mining", 10.0).shouldBeFalse()
        provider.takeExp(player, "mining", 10.0).shouldBeFalse()

        verify(exactly = 0) { mining.addEXP(any(), any(), any(), any()) }
    }

    private fun mockSkill(type: String, isLevelable: Boolean = true): Skill = mockk {
        every { this@mockk.type } returns type
        every { displayName } returns type
        every { isLevelableSkill } returns isLevelable
        every { maxLevel } returns MAX_LEVEL
        every { hasPermissionAccess(player) } returns true
        every { hasReachedLimit(player) } returns false
        every { expForLevel(any()) } answers { requiredExp(firstArg()) }
        every { addEXP(player, any(), any(), any()) } answers { if (!isChangeCancelled) addExp(secondArg()) }
    }

    private fun requiredExp(level: Int): Double = if (level <= MAX_LEVEL) 100.0 * level + 100 else -1.0

    /** Mirrors `Skill.addEXP` and `Skill.updateLevelUpConditions` of ValhallaMMO. */
    private fun addExp(amount: Double) {
        exp += amount
        if (exp < 0) {
            var nextLevel = level - 1
            for (i in nextLevel downTo 0) {
                val expForLevel = requiredExp(i)
                if (-exp >= expForLevel) {
                    nextLevel--
                    exp += expForLevel
                } else {
                    exp += expForLevel
                    break
                }
            }
            level = nextLevel.coerceAtLeast(0)
            if (level == 0) exp = exp.coerceAtLeast(0.0)
        } else {
            for (i in level + 1..MAX_LEVEL) {
                val expForLevel = requiredExp(i)
                if (exp < expForLevel) break
                level++
                exp -= expForLevel
            }
        }
    }

    private companion object {
        const val MAX_LEVEL = 10
    }
}
