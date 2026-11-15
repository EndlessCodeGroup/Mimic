// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.auraskills

import dev.aurelium.auraskills.api.user.SkillsUser
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.resources.ResourceState
import kotlin.test.Test

internal class AuraSkillsResourceProviderTest : BukkitTestBase() {

    private val user: SkillsUser = mockk(relaxUnitFun = true) {
        every { isLoaded } returns true
        every { mana } returns 15.5
        every { maxMana } returns 20.0
    }

    // SUT
    private val provider = AuraSkillsResourceProvider(
        mockk { every { api.getUser(player.uniqueId) } returns user },
    )

    @Test
    fun `when get primary resource - should return mana`() {
        provider.getPrimaryResource(player).id shouldBe "mana"
    }

    @Test
    fun `when get state - should return mana`() {
        provider.getState(player, "mana") shouldBe ResourceState.of(15.5, 20.0)
    }

    @Test
    fun `when mana is negative - should return zero`() {
        every { user.mana } returns -3.0

        provider.getState(player, "mana") shouldBe ResourceState.of(0.0, 20.0)
    }

    @Test
    fun `when set amount - should set mana without the not enough mana message`() {
        provider.setAmount(player, "mana", 5.5).shouldBeTrue()

        verify { user.mana = 5.5 }
        verify(exactly = 0) { user.consumeMana(any()) }
    }

    @Test
    fun `when user isn't loaded - should return nothing`() {
        every { user.isLoaded } returns false

        provider.getState(player, "mana").shouldBeNull()
        provider.setAmount(player, "mana", 5.0).shouldBeFalse()
    }

    @Test
    fun `when resource is unknown - should return nothing`() {
        provider.getState(player, "stamina").shouldBeNull()
        provider.setAmount(player, "stamina", 5.0).shouldBeFalse()
    }
}
