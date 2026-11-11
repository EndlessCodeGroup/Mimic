package ru.endlesscode.mimic

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.bukkit.entity.Player
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority.Lowest
import org.bukkit.plugin.ServicePriority.Normal
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.level.BukkitLevelSystem
import kotlin.test.Test

internal class MimicImplTest : BukkitTestBase() {

    private val enabledPlugins = mutableSetOf("Mimic", "ThirdParty")
    private val mimicPlugin = mockPlugin("Mimic")
    private val thirdParty = mockPlugin("ThirdParty")

    private var preferredLevelSystem = ""
    private val config: MimicConfig = mockk {
        every { levelSystem } answers { preferredLevelSystem }
    }

    // SUT
    private val mimic = MimicImpl(mimicPlugin, config)

    init {
        mimic.registerLevelSystem(TestLevelSystemProvider("minecraft"), MimicApiLevel.CURRENT, mimicPlugin, Lowest)
        mimic.registerLevelSystem(TestLevelSystemProvider("custom"), MimicApiLevel.CURRENT, thirdParty, Normal)
    }

    @Test
    fun `when preferred level system is not set - should use the one with the highest priority`() {
        mimic.getLevelSystemProvider().id shouldBe "custom"
    }

    @Test
    fun `when preferred level system is set - should use it`() {
        preferredLevelSystem = "minecraft"

        mimic.getLevelSystemProvider().id shouldBe "minecraft"
    }

    @Test
    fun `when preferred level system is unknown - should use the one with the highest priority`() {
        preferredLevelSystem = "unknown"

        mimic.getLevelSystemProvider().id shouldBe "custom"
    }

    @Test
    fun `when plugin of the level system is not enabled - should skip it`() {
        enabledPlugins -= "ThirdParty"

        mimic.getLevelSystemProvider().id shouldBe "minecraft"
    }

    @Test
    fun `when plugin is disabled - should unregister its services`() {
        mimic.onPluginDisable(PluginDisableEvent(thirdParty))

        mimic.getAllLevelSystemProviders().keys shouldContainExactly listOf("minecraft")
    }

    private fun mockPlugin(name: String): Plugin = mockk {
        every { this@mockk.name } returns name
        every { isEnabled } answers { name in enabledPlugins }
    }

    private class TestLevelSystemProvider(override val id: String) : BukkitLevelSystem.Provider {
        override fun getSystem(player: Player): BukkitLevelSystem = error("Not used")
    }
}
