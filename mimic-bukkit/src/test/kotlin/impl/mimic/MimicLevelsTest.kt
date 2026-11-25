// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mimic

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.ServicePriority.Lowest
import org.bukkit.plugin.ServicePriority.Normal
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.LookupStatus
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.level.LevelProgress
import ru.endlesscode.mimic.level.LevelProvider
import ru.endlesscode.mimic.level.LevelTrack
import kotlin.test.Test

internal class MimicLevelsTest : BukkitTestBase() {

    private val thirdParty = mockPlugin("ThirdParty")

    private val levelProviders = ServiceRegistry<LevelProvider>("LevelProvider", plugin, defaultId = "minecraft")
    private var preferredProviderId = ""

    // SUT
    private val levels = MimicLevels(levelProviders, preferredProviderId = { preferredProviderId })

    private val minecraft = TestLevelProvider(id = "minecraft", "level")
    private val skills = TestLevelProvider(id = "skills", "power", "mining", "profession/fishing")

    init {
        levelProviders.register(minecraft, plugin, Lowest)
        levelProviders.register(skills, thirdParty, Normal)
    }

    @Test
    fun `when get all tracks - should return namespaced tracks of all providers`() {
        levels.getAllTracks().keys() shouldContainExactly listOf(
            "skills:power",
            "skills:mining",
            "skills:profession/fishing",
            "minecraft:level",
        )
    }

    @Test
    fun `when get all tracks - should skip invalid and duplicated paths`() {
        val provider = TestLevelProvider(id = "broken", "valid", "Invalid", "with space", "", "ns:path", "valid")
        levelProviders.register(provider, thirdParty, Normal)

        val tracks = levels.getAllTracks().filter { it.key().namespace() == "broken" }
        tracks.keys() shouldHaveSingleElement "broken:valid"
    }

    @Test
    fun `when get player tracks - should namespace tracks returned by provider`() {
        skills.playerTracks = listOf("mining", "unknown", "mining", "Invalid")

        val tracks = levels.getTracks(player).filter { it.key().namespace() == "skills" }

        tracks.keys() shouldContainExactly listOf("skills:mining", "skills:unknown")
        tracks.names() shouldContainExactly listOf(Component.text("Name of mining"), Component.text("Name of unknown"))
    }

    @Test
    fun `when provider throws - should skip it`() {
        skills.failure = IllegalStateException("Test exception")

        levels.getAllTracks().keys() shouldHaveSingleElement "minecraft:level"
        levels.getProgress(player, key("skills:mining")).shouldBeNull()
        levels.setLevel(player, key("skills:mining"), 1).shouldBeFalse()
    }

    @Test
    fun `when preferred provider is not set - should use primary track of the provider with the highest priority`() {
        levels.getPrimaryTrack(player)?.key()?.asString() shouldBe "skills:power"
    }

    @Test
    fun `when preferred provider is set - should use its primary track`() {
        preferredProviderId = "minecraft"
        levels.getPrimaryTrack(player)?.key()?.asString() shouldBe "minecraft:level"
    }

    @Test
    fun `when compare tracks - should compare keys`() {
        val primaryTrack = levels.getPrimaryTrack(player)

        levels.getTracks(player).first() shouldBe primaryTrack
        levels.getAllTracks().toSet().size shouldBe 4
    }

    @Test
    fun `when preferred provider has no primary track - should return null`() {
        preferredProviderId = "skills"
        skills.primaryTrack = null
        levels.getPrimaryTrack(player).shouldBeNull()
    }

    @Test
    fun `when preferred provider is not set and the first one has no primary track - should use the next one`() {
        skills.primaryTrack = null
        levels.getPrimaryTrack(player)?.key()?.asString() shouldBe "minecraft:level"
    }

    @Test
    fun `when preferred provider is unknown - should use primary track of other providers`() {
        preferredProviderId = "unknown"
        levels.getPrimaryTrack(player)?.key()?.asString() shouldBe "skills:power"
    }

    @Test
    fun `when player is offline - should not call providers`() {
        every { player.isOnline } returns false

        levels.getTracks(player).shouldBeEmpty()
        levels.getPrimaryTrack(player).shouldBeNull()
        levels.getProgress(player, key("skills:mining")).shouldBeNull()
        levels.setLevel(player, key("skills:mining"), 1).shouldBeFalse()
        levels.giveExp(player, key("skills:mining"), 1.0).shouldBeFalse()
        levels.takeExp(player, key("skills:mining"), 1.0).shouldBeFalse()
        skills.writes.shouldBeEmpty()
    }

    @Test
    fun `when provider returns null track - should skip the provider`() {
        @Suppress("UNCHECKED_CAST") // Simulates a provider implemented in Java
        val tracks = listOf<LevelProvider.Track?>(null) as List<LevelProvider.Track>
        val javaProvider = object : LevelProvider {
            override val id: String = "java"
            override val allTracks: Collection<LevelProvider.Track> = tracks
            override fun getTracks(player: Player): Collection<LevelProvider.Track> = tracks
            override fun getPrimaryTrack(player: Player): LevelProvider.Track? = null
            override fun getProgress(player: Player, trackId: String): LevelProgress? = null
        }
        levelProviders.register(javaProvider, thirdParty, Normal)

        levels.getAllTracks().keys().filter { it.startsWith("java:") }.shouldBeEmpty()
        levels.getTracks(player).keys().filter { it.startsWith("java:") }.shouldBeEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = ["unknown:mining", "minecraft:mining", "mimic:mining"])
    fun `when track is unknown - should return null`(track: String) {
        levels.getProgress(player, key(track)).shouldBeNull()
        levels.setLevel(player, key(track), 1).shouldBeFalse()
    }

    @Test
    fun `when get progress - should use provider selected by namespace`() {
        levels.getProgress(player, key("skills:profession/fishing")) shouldBe skills.progress
    }

    @ParameterizedTest
    @CsvSource("4, true", "5, true", "6, false")
    fun `when check level - should check that current level is at least the given level`(level: Int, expected: Boolean) {
        levels.hasLevel(player, key("skills:mining"), level) shouldBe expected
    }

    @ParameterizedTest
    @CsvSource(
        "5,    5",
        "0,    1",
        "-10,  1",
        "100,  99",
    )
    fun `when set level - should clamp it`(level: Int, expected: Int) {
        levels.setLevel(player, key("skills:mining"), level).shouldBeTrue()
        skills.writes shouldContainExactly listOf("set mining $expected")
    }

    @Test
    fun `when set level and max level is unlimited - should not clamp it from above`() {
        skills.progress = progress(maxLevel = null)
        levels.setLevel(player, key("skills:mining"), 1000).shouldBeTrue()
        skills.writes shouldContainExactly listOf("set mining 1000")
    }

    @Test
    fun `when set level and player has no data - should not call provider`() {
        skills.progress = null
        levels.setLevel(player, key("skills:mining"), 5).shouldBeFalse()
        skills.writes.shouldBeEmpty()
    }

    @Test
    fun `when give and take exp - should pass amount to provider`() {
        levels.giveExp(player, key("skills:mining"), 10.0).shouldBeTrue()
        levels.takeExp(player, key("skills:mining"), 5.0).shouldBeTrue()
        skills.writes shouldContainExactly listOf("give mining 10.0", "take mining 5.0")
    }

    @ParameterizedTest
    @ValueSource(doubles = [-1.0, Double.NaN, Double.POSITIVE_INFINITY])
    fun `when exp amount is invalid - should throw`(amount: Double) {
        shouldThrow<IllegalArgumentException> { levels.giveExp(player, key("skills:mining"), amount) }
        shouldThrow<IllegalArgumentException> { levels.takeExp(player, key("skills:mining"), amount) }
    }

    @Test
    fun `when provider does not support writes - should return false`() {
        val readOnly = object : LevelProvider {
            override val id: String = "readonly"
            override val allTracks: Collection<LevelProvider.Track> = emptyList()
            override fun getTracks(player: Player): Collection<LevelProvider.Track> = allTracks
            override fun getPrimaryTrack(player: Player): LevelProvider.Track? = null
            override fun getProgress(player: Player, trackId: String): LevelProgress = progress()
        }
        levelProviders.register(readOnly, thirdParty, Normal)

        levels.setLevel(player, key("readonly:level"), 1).shouldBeFalse()
        levels.giveExp(player, key("readonly:level"), 1.0).shouldBeFalse()
        levels.takeExp(player, key("readonly:level"), 1.0).shouldBeFalse()
    }

    @Test
    fun `when use track of non-minecraft namespace - should not read all tracks`() {
        levels.getProgress(player, key("skills:mining"))
        levels.giveExp(player, key("skills:mining"), 1.0)
        skills.allTracksReads shouldBe 0
    }

    @ParameterizedTest
    @CsvSource(
        "Skills:Mining,   ,      FOUND,     skills:mining",
        "power,           ,      FOUND,     skills:power",
        "mining,          ,      AMBIGUOUS, ",
        "mining,          other, FOUND,     other:mining",
        "skills:unknown,  ,      NOT_FOUND, ",
    )
    fun `when look up track - should find it among tracks of providers`(
        value: String,
        preferred: String?,
        status: LookupStatus,
        expected: String?,
    ) {
        levelProviders.register(TestLevelProvider(id = "other", "mining"), thirdParty, Lowest)
        preferredProviderId = preferred.orEmpty()

        val result = levels.lookupTrack(value)

        result.status shouldBe status
        result.foundId shouldBe expected?.let(::key)
    }

    @Test
    fun `when look up track and provider throws - should not find it`() {
        skills.failure = IllegalStateException("Test exception")

        levels.lookupTrack("skills:mining").status shouldBe LookupStatus.NOT_FOUND
        levels.lookupTrack("mining").status shouldBe LookupStatus.NOT_FOUND
    }

    private fun key(id: String) = Key.key(id)

    private fun List<LevelTrack>.keys() = map { it.key().asString() }
    private fun List<LevelTrack>.names() = map { it.name() }

    private class TestLevelProvider(
        override val id: String,
        vararg paths: String,
    ) : LevelProvider {
        var failure: Throwable? = null
        var playerTracks: List<String> = paths.toList()
        var primaryTrack: String? = paths.first()
        var progress: LevelProgress? = progress()
        val writes = mutableListOf<String>()
        var allTracksReads = 0

        override val allTracks: Collection<LevelProvider.Track> = paths.map(::track)
            get() = checked { allTracksReads++; field }

        override fun getTracks(player: Player): Collection<LevelProvider.Track> = checked { playerTracks.map(::track) }
        override fun getPrimaryTrack(player: Player): LevelProvider.Track? = checked { primaryTrack?.let(::track) }
        override fun getProgress(player: Player, trackId: String): LevelProgress? =
            checked { progress.takeIf { trackId in playerTracks } }

        override fun setLevel(player: Player, trackId: String, level: Int): Boolean = write("set $trackId $level")
        override fun giveExp(player: Player, trackId: String, amount: Double): Boolean = write("give $trackId $amount")
        override fun takeExp(player: Player, trackId: String, amount: Double): Boolean = write("take $trackId $amount")

        private fun track(path: String) = LevelProvider.Track(path, Component.text("Name of $path"))

        private fun write(operation: String): Boolean = checked { writes += operation; true }

        private inline fun <T> checked(block: () -> T): T {
            failure?.let { throw it }
            return block()
        }
    }

    private companion object {
        fun progress(maxLevel: Int? = 99) = LevelProgress.level(5)
            .minLevel(1)
            .apply { if (maxLevel != null) maxLevel(maxLevel) }
            .exp(10.0)
            .expToNextLevel(100.0)
            .build()
    }
}
