// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.heroes

import com.herocraftonline.heroes.characters.Hero
import com.herocraftonline.heroes.characters.classes.HeroClass
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.level.LevelProgress
import kotlin.test.Test

internal class HeroesLevelProviderTest : BukkitTestBase() {

    private val warrior = mockClass("Warrior", maxLevel = 10)
    private val darkKnight = mockClass("Dark Knight", maxLevel = 3)

    /** Total experience by class name, like Heroes stores it. */
    private val experience = mutableMapOf<String, Double>()
    private var isLoaded = true

    private val hero: Hero = mockk(relaxUnitFun = true) {
        every { heroClass } returns warrior
        every { secondaryClass } returns null
        every { raceClass } returns null
        every { experienceMap } returns experience
        every { getExperience(any<HeroClass>()) } answers { experience[firstArg<HeroClass>().name] ?: 0.0 }
        every { getHeroLevel(any<HeroClass>()) } answers { levelFor(experience[firstArg<HeroClass>().name] ?: 0.0) }
        every { addExp(any(), any(), any()) } answers {
            val heroClass = secondArg<HeroClass>()
            experience[heroClass.name] = ((experience[heroClass.name] ?: 0.0) + firstArg<Double>()).coerceAtLeast(0.0)
        }
    }

    private val heroes: HeroesWrapper = mockk {
        every { getLoadedHero(player) } answers { if (isLoaded) hero else null }
        every { getRealClasses() } returns listOf(darkKnight, warrior)
        every { getTotalExp(any()) } answers { totalExp(firstArg()) }
    }

    // SUT
    private val provider = HeroesLevelProvider(heroes)

    init {
        every { player.location } returns mockk()
    }

    @Test
    fun `when get all tracks - should return classes`() {
        provider.allTracks.map { it.id } shouldContainExactly listOf("dark_knight", "warrior")
    }

    @Test
    fun `when get tracks - should return current classes and classes with exp`() {
        experience["Dark Knight"] = 0.0

        provider.getTracks(player).map { it.id } shouldContainExactly listOf("dark_knight", "warrior")
    }

    @Test
    fun `when get primary track - should return primary class`() {
        provider.getPrimaryTrack(player)?.id shouldBe "warrior"
    }

    @Test
    fun `when hero isn't loaded - should return nothing`() {
        isLoaded = false

        provider.getPrimaryTrack(player).shouldBeNull()
        provider.getProgress(player, "warrior").shouldBeNull()
        provider.giveExp(player, "warrior", 10.0).shouldBeFalse()
    }

    @Test
    fun `when hero doesn't have the class - should not support it`() {
        provider.getProgress(player, "dark_knight").shouldBeNull()
        provider.setLevel(player, "dark_knight", 2).shouldBeFalse()
        provider.giveExp(player, "dark_knight", 10.0).shouldBeFalse()
        provider.takeExp(player, "dark_knight", 10.0).shouldBeFalse()
    }

    @Test
    fun `when get progress - should return exp within the level`() {
        experience["Warrior"] = 150.0

        provider.getProgress(player, "warrior") shouldBe
            LevelProgress.level(2).minLevel(1).maxLevel(10).exp(50.0).expToNextLevel(200.0).build()
    }

    @Test
    fun `when get progress at max level - should not have exp to next level`() {
        experience["Dark Knight"] = 300.0

        provider.getProgress(player, "dark_knight") shouldBe
            LevelProgress.level(3).minLevel(1).maxLevel(3).exp(0.0).build()
    }

    @Test
    fun `when set level - should add exp difference`() {
        experience["Warrior"] = 150.0

        provider.setLevel(player, "warrior", 4).shouldBeTrue()

        verify { hero.addExp(450.0, warrior, any()) }
        experience["Warrior"] shouldBe 600.0
    }

    @Test
    fun `when give exp - should add exp`() {
        experience["Warrior"] = 150.0

        provider.giveExp(player, "warrior", 30.0).shouldBeTrue()

        verify { hero.addExp(30.0, warrior, any()) }
    }

    @Test
    fun `when give exp above max level - should give exp up to max level`() {
        experience["Dark Knight"] = 250.0

        provider.giveExp(player, "dark_knight", 100.0).shouldBeTrue()

        verify { hero.addExp(50.0, darkKnight, any()) }
    }

    @Test
    fun `when take exp - should add negative exp`() {
        provider.takeExp(player, "warrior", 40.0).shouldBeTrue()

        verify { hero.addExp(-40.0, warrior, any()) }
    }

    private fun mockClass(name: String, maxLevel: Int): HeroClass = mockk {
        every { this@mockk.name } returns name
        every { this@mockk.maxLevel } returns maxLevel
    }

    /** Total experience to reach a level: 0, 100, 300, 600... */
    private fun totalExp(level: Int): Int = if (level <= 1) 0 else 50 * level * (level - 1)

    private fun levelFor(exp: Double): Int = (1..100).last { totalExp(it) <= exp }
}
