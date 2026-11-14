// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.auraskills

import dev.aurelium.auraskills.api.AuraSkillsApi
import dev.aurelium.auraskills.api.registry.NamespacedId
import dev.aurelium.auraskills.api.skill.Skill
import dev.aurelium.auraskills.api.user.SkillsUser
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
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.level.LevelProgress
import java.util.Locale
import kotlin.test.Test

internal class AuraSkillsLevelProviderTest : BukkitTestBase() {

    private val mining = mockSkill(NamespacedId.of("auraskills", "mining"), maxLevel = 50)
    private val fishing = mockSkill(NamespacedId.of("myaddon", "fishing"), maxLevel = 30)
    private val disabled = mockSkill(NamespacedId.of("auraskills", "disabled"), maxLevel = 10, isEnabled = false)

    private val user: SkillsUser = mockk(relaxUnitFun = true) {
        every { isLoaded } returns true
    }

    private var startLevel = 0

    private val api: AuraSkillsApi = mockk {
        every { getUser(player.uniqueId) } returns user
        every { globalRegistry.skills } returns listOf(mining, fishing, disabled)
        every { mainConfig.startLevel } answers { startLevel }
        every { messageManager.defaultLanguage } returns Locale.ENGLISH
        // Mirrors AuraSkills: default requirements 100 * i^2 + 100, indexed from the start level, 0 beyond the list
        every { xpRequirements.getXpRequired(any(), any()) } answers {
            val index = secondArg<Int>() - startLevel - 1
            when {
                index < 0 -> throw IndexOutOfBoundsException(index)
                index >= REQUIREMENTS_SIZE -> 0
                else -> 100 * index * index + 100
            }
        }
    }

    // SUT
    private val provider = AuraSkillsLevelProvider { api }

    @Test
    fun `when get all tracks - should return enabled skills and power`() {
        provider.allTracks.map { it.id } shouldContainExactly listOf("mining", "myaddon/fishing", "power")
    }

    @Test
    fun `when get tracks of loaded player - should return all tracks`() {
        provider.getTracks(player).map { it.id } shouldContainExactly listOf("mining", "myaddon/fishing", "power")
    }

    @Test
    fun `when get tracks of not loaded player - should return empty list`() {
        every { user.isLoaded } returns false

        provider.getTracks(player).shouldBeEmpty()
    }

    @Test
    fun `when get primary track - should return power`() {
        provider.getPrimaryTrack(player).id shouldBe "power"
    }

    @Test
    fun `when get skill progress - should return level and exp`() {
        every { user.getSkillLevel(mining) } returns 2
        every { user.getSkillXp(mining) } returns 150.0

        provider.getProgress(player, "mining") shouldBe
            LevelProgress.level(2).minLevel(0).maxLevel(50).exp(150.0).expToNextLevel(500.0).build()
    }

    @Test
    fun `when get progress of skill with namespace - should find skill`() {
        every { user.getSkillLevel(fishing) } returns 0
        every { user.getSkillXp(fishing) } returns 0.0

        provider.getProgress(player, "myaddon/fishing") shouldBe
            LevelProgress.level(0).minLevel(0).maxLevel(30).exp(0.0).expToNextLevel(100.0).build()
    }

    @Test
    fun `when get progress at max level - should not have exp to next level`() {
        every { user.getSkillLevel(mining) } returns 50
        every { user.getSkillXp(mining) } returns 0.0

        provider.getProgress(player, "mining") shouldBe
            LevelProgress.level(50).minLevel(0).maxLevel(50).exp(0.0).build()
    }

    @Test
    fun `when get progress beyond xp requirements - should not have exp to next level`() {
        every { user.getSkillLevel(mining) } returns 10
        every { user.getSkillXp(mining) } returns 0.0
        every { api.xpRequirements.getXpRequired(mining, 11) } returns 0

        provider.getProgress(player, "mining") shouldBe
            LevelProgress.level(10).minLevel(0).maxLevel(50).exp(0.0).build()
    }

    @Test
    fun `when get progress with start level 1 - should use it as min level`() {
        startLevel = 1
        every { user.getSkillLevel(mining) } returns 1
        every { user.getSkillXp(mining) } returns 0.0

        provider.getProgress(player, "mining") shouldBe
            LevelProgress.level(1).minLevel(1).maxLevel(50).exp(0.0).expToNextLevel(100.0).build()
    }

    @Test
    fun `when get progress below start level - should report start level`() {
        startLevel = 1
        every { user.getSkillLevel(mining) } returns 0
        every { user.getSkillXp(mining) } returns 0.0

        provider.getProgress(player, "mining") shouldBe
            LevelProgress.level(1).minLevel(1).maxLevel(50).exp(0.0).expToNextLevel(100.0).build()
    }

    @Test
    fun `when get power progress below start level - should report min level`() {
        startLevel = 1
        every { user.powerLevel } returns 1

        provider.getProgress(player, "power") shouldBe
            LevelProgress.level(2).minLevel(2).maxLevel(80).build()
    }

    @Test
    fun `when get progress above max level - should not have exp to next level`() {
        every { user.getSkillLevel(mining) } returns 60
        every { user.getSkillXp(mining) } returns 0.0

        provider.getProgress(player, "mining") shouldBe
            LevelProgress.level(60).minLevel(0).maxLevel(50).exp(0.0).build()
    }

    @Test
    fun `when get power progress - should return sum of levels without exp`() {
        every { user.powerLevel } returns 7

        provider.getProgress(player, "power") shouldBe
            LevelProgress.level(7).minLevel(0).maxLevel(80).build()
    }

    @Test
    fun `when get power progress with start level 1 - should start from the number of skills`() {
        startLevel = 1
        every { user.powerLevel } returns 5

        provider.getProgress(player, "power") shouldBe
            LevelProgress.level(5).minLevel(2).maxLevel(80).build()
    }

    @Test
    fun `when get progress of not loaded player - should return null`() {
        every { user.isLoaded } returns false

        provider.getProgress(player, "mining").shouldBeNull()
    }

    @Test
    fun `when get progress of disabled or unknown skill - should return null`() {
        provider.getProgress(player, "disabled").shouldBeNull()
        provider.getProgress(player, "unknown").shouldBeNull()
    }

    @Test
    fun `when set level - should reset exp`() {
        provider.setLevel(player, "mining", 10).shouldBeTrue()

        verifyOrder {
            user.setSkillLevel(mining, 10)
            user.setSkillXp(mining, 0.0)
        }
    }

    @Test
    fun `when change power - should not be supported`() {
        provider.setLevel(player, "power", 10).shouldBeFalse()
        provider.giveExp(player, "power", 10.0).shouldBeFalse()
        provider.takeExp(player, "power", 10.0).shouldBeFalse()
    }

    @Test
    fun `when give exp - should add exact exp`() {
        provider.giveExp(player, "mining", 42.0).shouldBeTrue()

        verify { user.addSkillXpRaw(mining, 42.0) }
    }

    @Test
    fun `when take exp within level - should keep level`() {
        every { user.getSkillLevel(mining) } returns 2
        every { user.getSkillXp(mining) } returns 150.0

        provider.takeExp(player, "mining", 50.0).shouldBeTrue()

        verify(exactly = 0) { user.setSkillLevel(any(), any()) }
        verify { user.setSkillXp(mining, 100.0) }
    }

    @Test
    fun `when take exp above level exp - should lower level`() {
        every { user.getSkillLevel(mining) } returns 2
        every { user.getSkillXp(mining) } returns 50.0

        // 50 exp on level 2, then 200 exp of the step from level 1 to 2
        provider.takeExp(player, "mining", 100.0).shouldBeTrue()

        verifyOrder {
            user.setSkillLevel(mining, 1)
            user.setSkillXp(mining, 150.0)
        }
    }

    @Test
    fun `when take more exp than total - should stop at start level`() {
        every { user.getSkillLevel(mining) } returns 2
        every { user.getSkillXp(mining) } returns 50.0

        provider.takeExp(player, "mining", 1000.0).shouldBeTrue()

        verifyOrder {
            user.setSkillLevel(mining, 0)
            user.setSkillXp(mining, 0.0)
        }
    }

    @Test
    fun `when take exp with start level 1 - should stop at start level`() {
        startLevel = 1
        every { user.getSkillLevel(mining) } returns 2
        every { user.getSkillXp(mining) } returns 50.0

        // 50 exp on level 2, then 100 exp of the step from level 1 to 2
        provider.takeExp(player, "mining", 100.0).shouldBeTrue()

        verifyOrder {
            user.setSkillLevel(mining, 1)
            user.setSkillXp(mining, 50.0)
        }
    }

    @Test
    fun `when take exp above xp requirements - should keep level`() {
        every { user.getSkillLevel(mining) } returns 60
        every { user.getSkillXp(mining) } returns 0.0

        provider.takeExp(player, "mining", 1.0).shouldBeTrue()

        verify(exactly = 0) { user.setSkillLevel(any(), any()) }
        verify { user.setSkillXp(mining, 0.0) }
    }

    @Test
    fun `when change level of not loaded player - should return false`() {
        every { user.isLoaded } returns false

        provider.setLevel(player, "mining", 10).shouldBeFalse()
        provider.giveExp(player, "mining", 10.0).shouldBeFalse()
        provider.takeExp(player, "mining", 10.0).shouldBeFalse()
    }

    private fun mockSkill(id: NamespacedId, maxLevel: Int, isEnabled: Boolean = true): Skill = mockk {
        every { this@mockk.id } returns id
        every { this@mockk.maxLevel } returns maxLevel
        every { this@mockk.isEnabled } returns isEnabled
        every { getDisplayName(any(), false) } returns id.key
    }

    private companion object {
        /** AuraSkills has requirements up to the highest max level. */
        const val REQUIREMENTS_SIZE = 50
    }
}
