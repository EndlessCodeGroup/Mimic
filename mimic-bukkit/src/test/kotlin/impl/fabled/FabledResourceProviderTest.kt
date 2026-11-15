// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.fabled

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.resources.ResourceState
import studio.magemonkey.fabled.api.enums.ManaCost
import studio.magemonkey.fabled.api.enums.ManaSource
import studio.magemonkey.fabled.api.player.PlayerData
import kotlin.test.Test

internal class FabledResourceProviderTest : BukkitTestBase() {

    private var isLoaded = true
    private var isManaEnabled = true

    private val data: PlayerData = mockk(relaxUnitFun = true) {
        every { mana } returns 30.0
        every { maxMana } returns 100.0
    }

    // SUT
    private val provider = FabledResourceProvider(
        mockk {
            every { getLoadedData(player) } answers { if (isLoaded) data else null }
            every { isEnabled } returns true
            every { this@mockk.isManaEnabled } answers { this@FabledResourceProviderTest.isManaEnabled }
        },
    )

    @Test
    fun `when mana is disabled in Fabled - should be disabled`() {
        provider.isEnabled.shouldBeTrue()

        isManaEnabled = false
        provider.isEnabled.shouldBeFalse()
    }

    @Test
    fun `when get primary resource - should return mana`() {
        provider.getPrimaryResource(player).id shouldBe "mana"
    }

    @Test
    fun `when get state - should return mana`() {
        provider.getState(player, "mana") shouldBe ResourceState.of(30.0, 100.0)
    }

    @Test
    fun `when set lower amount - should use mana with event`() {
        provider.setAmount(player, "mana", 10.0).shouldBeTrue()

        verify { data.useMana(20.0, ManaCost.SPECIAL) }
    }

    @Test
    fun `when set higher amount - should give mana with event`() {
        provider.setAmount(player, "mana", 45.5).shouldBeTrue()

        verify { data.giveMana(15.5, ManaSource.SPECIAL) }
    }

    @Test
    fun `when set the same amount - should change nothing`() {
        provider.setAmount(player, "mana", 30.0).shouldBeTrue()

        verify(exactly = 0) {
            data.useMana(any(), any())
            data.giveMana(any(), any())
        }
    }

    @Test
    fun `when data isn't loaded - should return nothing`() {
        isLoaded = false

        provider.getState(player, "mana").shouldBeNull()
        provider.setAmount(player, "mana", 5.0).shouldBeFalse()
    }

    @Test
    fun `when resource is unknown - should return nothing`() {
        provider.getState(player, "stamina").shouldBeNull()
        provider.setAmount(player, "stamina", 5.0).shouldBeFalse()
    }
}
