// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic

import io.kotest.matchers.collections.shouldBeEmpty
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority.Normal
import org.bukkit.plugin.SimpleServicesManager
import ru.endlesscode.mimic.level.BukkitLevelSystem
import kotlin.test.Test

internal class ServicesRegistrationListenerTest : BukkitTestBase() {

    private val servicesManager = SimpleServicesManager()
    private val mimic: Mimic = mockk {
        every { registerLevelSystem(any(), any(), any(), any()) } answers { firstArg() }
    }
    private val thirdParty: Plugin = mockk {
        every { name } returns "ThirdParty"
        every { pluginMeta } returns mockk {
            every { authors } returns listOf("Plugin Author")
        }
    }

    // SUT
    private val listener = ServicesRegistrationListener(servicesManager, mimic)

    @Test
    fun `when forward registered services - should move them from services manager to Mimic`() {
        val provider = TestLevelSystemProvider()
        servicesManager.register(BukkitLevelSystem.Provider::class.java, provider, thirdParty, Normal)

        listener.forwardRegisteredServices()

        verify { mimic.registerLevelSystem(provider, MimicApiLevel.VERSION_0_7, thirdParty, Normal) }
        servicesManager.getRegistrations(BukkitLevelSystem.Provider::class.java).shouldBeEmpty()
    }

    private class TestLevelSystemProvider : BukkitLevelSystem.Provider {
        override val id: String = "custom"
        override fun getSystem(player: Player): BukkitLevelSystem = error("Not used")
    }
}
