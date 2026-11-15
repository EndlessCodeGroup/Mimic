// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.heroes

import com.herocraftonline.heroes.characters.Hero
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.resources.ResourceState
import kotlin.test.Test

internal class HeroesResourceProviderTest : BukkitTestBase() {

    private var isLoaded = true

    private val hero: Hero = mockk(relaxUnitFun = true) {
        every { mana } returns 30
        every { maxMana } returns 100
        every { stamina } returns 500
        every { maxStamina } returns 1000
    }

    // SUT
    private val provider = HeroesResourceProvider(
        mockk { every { getLoadedHero(player) } answers { if (isLoaded) hero else null } },
    )

    @Test
    fun `when get resources - should return mana as primary and stamina`() {
        provider.allResources.map { it.id } shouldContainExactly listOf("mana", "stamina")
        provider.getPrimaryResource(player).id shouldBe "mana"
    }

    @Test
    fun `when get state - should return mana and stamina`() {
        provider.getState(player, "mana") shouldBe ResourceState.of(30.0, 100.0)
        provider.getState(player, "stamina") shouldBe ResourceState.of(500.0, 1000.0)
    }

    @Test
    fun `when mana is negative - should return zero`() {
        every { hero.mana } returns -5

        provider.getState(player, "mana") shouldBe ResourceState.of(0.0, 100.0)
    }

    @Test
    fun `when set amount - should round it down`() {
        provider.setAmount(player, "mana", 25.9).shouldBeTrue()
        provider.setAmount(player, "stamina", 499.5).shouldBeTrue()

        verify {
            hero.mana = 25
            hero.stamina = 499
        }
    }

    @Test
    fun `when hero isn't loaded - should return nothing`() {
        isLoaded = false

        provider.getState(player, "mana").shouldBeNull()
        provider.setAmount(player, "mana", 5.0).shouldBeFalse()
    }

    @Test
    fun `when resource is unknown - should return nothing`() {
        provider.getState(player, "rage").shouldBeNull()
        provider.setAmount(player, "rage", 5.0).shouldBeFalse()
    }
}
