// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.heroes

import com.herocraftonline.heroes.api.events.AfterClassChangeEvent
import com.herocraftonline.heroes.api.events.ClassChangeEvent
import com.herocraftonline.heroes.characters.Hero
import com.herocraftonline.heroes.characters.classes.HeroClass
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

internal class HeroesClassProviderTest : BukkitTestBase() {

    private val vagrant = mockClass("Vagrant", isPrimary = true, isDefault = true)
    private val warrior = mockClass("Warrior", isPrimary = true)
    private val knight = mockClass("Knight", isPrimary = true)
    private val darkKnight = mockClass(
        "Dark Knight",
        isPrimary = true,
        strongParents = setOf(warrior),
        weakParents = setOf(knight, vagrant),
    )

    // Heroes makes classes primary by default, so professions are usually primary too
    private val miner = mockClass("Miner", isPrimary = true, isSecondary = true)
    private val human = mockClass("Human", isRace = true, isDefaultRace = true)
    private val elf = mockClass("Elf", isRace = true)

    private var isLoaded = true
    private val events = mutableListOf<ClassChangeEvent>()
    private val afterEvents = mutableListOf<AfterClassChangeEvent>()

    private val hero: Hero = mockk(relaxUnitFun = true) {
        every { player } returns this@HeroesClassProviderTest.player
        every { heroClass } returns darkKnight
        every { secondaryClass } returns null
        every { raceClass } returns human
    }

    private val heroes: HeroesWrapper = mockk {
        every { getLoadedHero(player) } answers { if (isLoaded) hero else null }
        every { getRealClasses() } returns listOf(vagrant, warrior, knight, darkKnight, miner, human, elf)
        every { callEvent(any()) } answers {
            when (val event = firstArg<Any>()) {
                is ClassChangeEvent -> events += event
                is AfterClassChangeEvent -> afterEvents += event
            }
        }
    }

    // SUT
    private val provider = HeroesClassProvider(heroes)

    @Test
    fun `when get all classes - should skip the default class and keep the default race`() {
        val classes = provider.allClasses

        classes.map { it.id } shouldContainExactly listOf("warrior", "knight", "dark_knight", "miner", "human", "elf")
        classes.single { it.id == "dark_knight" }.parents shouldBe setOf("warrior", "knight")
        classes.single { it.id == "dark_knight" }.levelTrack shouldBe "dark_knight"
    }

    @Test
    fun `when get classes - should skip empty slots and keep the default race`() {
        provider.getClasses(player).mapValues { it.value.id } shouldBe mapOf("primary" to "dark_knight", "race" to "human")

        every { hero.secondaryClass } returns miner
        every { hero.raceClass } returns elf
        provider.getClasses(player).mapValues { it.value.id } shouldBe mapOf(
            "primary" to "dark_knight",
            "secondary" to "miner",
            "race" to "elf",
        )
    }

    @Test
    fun `when hero is not loaded - should return no classes and not set class`() {
        isLoaded = false

        provider.getClasses(player).shouldBeEmpty()
        provider.setClass(player, "warrior", slotId = null).shouldBeFalse()
    }

    @Test
    fun `when set class without slot - should set it into the slot of its type`() {
        provider.setClass(player, "elf", slotId = null).shouldBeTrue()

        events.single().from shouldBe human
        events.single().to shouldBe elf
        verify { hero.changeHeroClass(elf, false, true) }
        afterEvents.single().to shouldBe elf
    }

    @Test
    fun `when set profession without slot - should prefer secondary slot to primary`() {
        provider.setClass(player, "miner", slotId = null).shouldBeTrue()
        verify { hero.changeHeroClass(miner, true, false) }
    }

    @Test
    fun `when set class into any of its slots - should set it`() {
        provider.setClass(player, "miner", slotId = "primary").shouldBeTrue()
        verify { hero.changeHeroClass(miner, false, false) }
    }

    @Test
    fun `when set class into a slot of another type - should not set it`() {
        provider.setClass(player, "elf", slotId = "primary").shouldBeFalse()
        provider.setClass(player, "warrior", slotId = "unknown").shouldBeFalse()
        events.shouldBeEmpty()
    }

    @Test
    fun `when player has the default class - should report empty slot`() {
        every { hero.heroClass } returns vagrant
        provider.getClasses(player).keys shouldBe setOf("race")
    }

    @Test
    fun `when set the current class - should do nothing`() {
        provider.setClass(player, "dark_knight", slotId = null).shouldBeTrue()
        events.shouldBeEmpty()
    }

    @Test
    fun `when set unknown or default class - should not set it`() {
        provider.setClass(player, "unknown", slotId = null).shouldBeFalse()
        provider.setClass(player, "vagrant", slotId = null).shouldBeFalse()
        events.shouldBeEmpty()
    }

    @Suppress("LongParameterList")
    private fun mockClass(
        name: String,
        isPrimary: Boolean = false,
        isSecondary: Boolean = false,
        isRace: Boolean = false,
        isDefault: Boolean = false,
        isDefaultRace: Boolean = false,
        strongParents: Set<HeroClass> = emptySet(),
        weakParents: Set<HeroClass> = emptySet(),
    ): HeroClass = mockk {
        every { this@mockk.name } returns name
        every { this@mockk.isPrimary } returns isPrimary
        every { this@mockk.isSecondary } returns isSecondary
        every { this@mockk.isRace } returns isRace
        every { this@mockk.isDefault } returns isDefault
        every { isDefaultProf } returns false
        every { this@mockk.isDefaultRace } returns isDefaultRace
        every { this@mockk.strongParents } returns strongParents
        every { this@mockk.weakParents } returns weakParents
    }
}
