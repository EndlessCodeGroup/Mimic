// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mmocore

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.lumine.mythic.lib.player.resource.ResourceUpdateReason
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.Indyuce.mmocore.api.player.PlayerData
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.resources.ResourceState
import kotlin.test.Test

internal class MmoCoreResourceProviderTest : BukkitTestBase() {

    private var isReady = true

    private val data: PlayerData = mockk(relaxUnitFun = true) {
        every { mana } returns 30.5
        every { stamina } returns 8.0
        every { stellium } returns 0.0
        every { stats.getStat("MAX_MANA") } returns 100.0
        every { stats.getStat("MAX_STAMINA") } returns 20.0
        every { stats.getStat("MAX_STELLIUM") } returns 50.0
        every { setMana(any(), any<ResourceUpdateReason>()) } returns true
        every { setStamina(any(), any<ResourceUpdateReason>()) } returns true
    }

    // SUT
    private val provider = MmoCoreResourceProvider(
        mockk { every { getLoadedData(player) } answers { if (isReady) data else null } },
    )

    @Test
    fun `when get resources - should return mana as primary, stamina and stellium`() {
        provider.allResources.map { it.id } shouldContainExactly listOf("mana", "stamina", "stellium")
        provider.getPrimaryResource(player).id shouldBe "mana"
    }

    @Test
    fun `when get state - should return current and max values`() {
        provider.getState(player, "mana") shouldBe ResourceState.of(30.5, 100.0)
        provider.getState(player, "stamina") shouldBe ResourceState.of(8.0, 20.0)
        provider.getState(player, "stellium") shouldBe ResourceState.of(0.0, 50.0)
    }

    @Test
    fun `when set amount - should set it with event`() {
        provider.setAmount(player, "mana", 12.5).shouldBeTrue()
        provider.setAmount(player, "stamina", 3.0).shouldBeTrue()

        verify {
            data.setMana(12.5, ResourceUpdateReason.OTHER)
            data.setStamina(3.0, ResourceUpdateReason.OTHER)
        }
    }

    @Test
    fun `when data isn't ready - should return nothing`() {
        isReady = false

        provider.getState(player, "mana").shouldBeNull()
        provider.setAmount(player, "mana", 5.0).shouldBeFalse()
    }

    @Test
    fun `when resource is unknown - should return nothing`() {
        provider.getState(player, "health").shouldBeNull()
        provider.setAmount(player, "health", 5.0).shouldBeFalse()
    }
}
