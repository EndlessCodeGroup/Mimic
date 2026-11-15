// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // LevelSystem API is deprecated but still supported

package ru.endlesscode.mimic

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority.*
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.impl.mimic.MimicLevels
import ru.endlesscode.mimic.impl.vanilla.MinecraftLevelProvider
import ru.endlesscode.mimic.level.*
import kotlin.test.Test

internal class MimicImplTest : BukkitTestBase() {

    private val enabledPlugins = mutableSetOf("Mimic", "ThirdParty", "Other")
    private val mimicPlugin = mockPlugin("Mimic")
    private val thirdParty = mockPlugin("ThirdParty")
    private val otherPlugin = mockPlugin("Other")

    private var preferredLevelProvider = ""
    private val config: MimicConfig = mockk {
        every { levelProvider } answers { preferredLevelProvider }
    }

    // SUT
    private val mimic = MimicImpl(mimicPlugin, config)

    init {
        mimic.registerLevelSystem(TestLevelSystemProvider("minecraft"), MimicApiLevel.CURRENT, mimicPlugin, Lowest)
        mimic.registerLevelProvider(MinecraftLevelProvider(), MimicApiLevel.CURRENT, mimicPlugin, Lowest)
        mimic.registerLevelSystem(TestLevelSystemProvider("custom"), MimicApiLevel.CURRENT, thirdParty, Normal)
    }

    @Test
    fun `when preferred level system is not set - should use the one with the highest priority`() {
        mimic.getLevelSystemProvider().id shouldBe "custom"
    }

    @Test
    fun `when preferred level system is set - should use it`() {
        preferredLevelProvider = "minecraft"

        mimic.getLevelSystemProvider().id shouldBe "minecraft"
    }

    @Test
    fun `when preferred level system is unknown - should use the one with the highest priority`() {
        preferredLevelProvider = "unknown"

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

    @Test
    fun `when preferred level system is a level provider - should use the level system with the highest priority`() {
        mimic.registerLevelProvider(TestLevelProvider("tracks"), MimicApiLevel.CURRENT, thirdParty, Normal)
        preferredLevelProvider = "tracks"

        mimic.getLevelSystemProvider().id shouldBe "custom"
    }

    @Test
    fun `when level provider is set in config - should use its primary track`() {
        mimic.registerLevelProvider(TestLevelProvider("tracks"), MimicApiLevel.CURRENT, thirdParty, Lowest)
        preferredLevelProvider = "tracks"

        mimic.levels.getPrimaryTrack(player)?.key()?.asString() shouldBe "tracks:level"
    }

    @Test
    fun `when plugin is disabled - should unregister its level providers`() {
        mimic.registerLevelProvider(TestLevelProvider("tracks"), MimicApiLevel.CURRENT, thirdParty, Normal)

        mimic.onPluginDisable(PluginDisableEvent(thirdParty))

        mimic.levels.getAllTracks().map { it.key().asString() } shouldContainExactly listOf("minecraft:level")
    }

    @Test
    fun `when level system is registered - should provide it as track level`() {
        mimic.levels.getAllTracks().keys() shouldContainExactly listOf("custom:level", "minecraft:level")
        mimic.levels.getPrimaryTrack(player)?.key()?.asString() shouldBe "custom:level"
    }

    @Test
    fun `when level system is registered after level provider with the same ID - should keep level provider`() {
        mimic.registerLevelProvider(TestLevelProvider("tracks"), MimicApiLevel.CURRENT, thirdParty, Normal)
        mimic.registerLevelSystem(TestLevelSystemProvider("tracks"), MimicApiLevel.CURRENT, thirdParty, Normal)

        mimic.levels.trackName("tracks:level") shouldBe Component.empty()
    }

    @Test
    fun `when level provider is registered after level system with the same ID - should replace it`() {
        mimic.registerLevelSystem(TestLevelSystemProvider("tracks"), MimicApiLevel.CURRENT, thirdParty, Normal)
        mimic.registerLevelProvider(TestLevelProvider("tracks"), MimicApiLevel.CURRENT, thirdParty, Normal)

        mimic.levels.trackName("tracks:level") shouldBe Component.empty()
    }

    @Test
    fun `when level provider is registered after level system by another plugin - should replace it`() {
        mimic.registerLevelSystem(TestLevelSystemProvider("tracks"), MimicApiLevel.CURRENT, thirdParty, High)
        mimic.registerLevelProvider(TestLevelProvider("tracks"), MimicApiLevel.CURRENT, otherPlugin, Normal)

        mimic.levels.trackName("tracks:level") shouldBe Component.empty()
    }

    @Test
    fun `when level system is replaced - should provide the new one`() {
        mimic.registerLevelSystem(TestLevelSystemProvider("builtin"), MimicApiLevel.CURRENT, mimicPlugin, Normal)
        mimic.registerLevelSystem(TestLevelSystemProvider("builtin"), MimicApiLevel.CURRENT, thirdParty, Normal)

        (mimic.levels as MimicLevels).providers.enabled.single { it.service.id == "builtin" }.plugin shouldBe thirdParty
    }

    private fun List<LevelTrack>.keys() = map { it.key().asString() }

    private fun Levels.trackName(id: String) = getAllTracks().single { it.key().asString() == id }.name()

    private fun mockPlugin(name: String): Plugin = mockk {
        every { this@mockk.name } returns name
        every { isEnabled } answers { name in enabledPlugins }
        every { pluginMeta } returns mockk { every { authors } returns emptyList() }
    }

    private class TestLevelProvider(override val id: String) : LevelProvider {
        private val track = LevelProvider.Track("level", Component.empty())
        override val allTracks: Collection<LevelProvider.Track> = listOf(track)
        override fun getTracks(player: Player): Collection<LevelProvider.Track> = allTracks
        override fun getPrimaryTrack(player: Player): LevelProvider.Track = track
        override fun getProgress(player: Player, trackId: String): LevelProgress? = null
    }

    private class TestLevelSystemProvider(override val id: String) : BukkitLevelSystem.Provider {
        override fun getSystem(player: Player): BukkitLevelSystem = error("Not used")
    }
}
