// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mimic

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import net.kyori.adventure.key.Key
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority.Low
import org.bukkit.plugin.ServicePriority.Lowest
import org.bukkit.plugin.ServicePriority.Normal
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.LookupResult
import ru.endlesscode.mimic.LookupStatus
import ru.endlesscode.mimic.MimicService
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.internal.Log
import kotlin.test.Test

internal class NamespacedProvidersTest : BukkitTestBase() {

    private var enabledPlugins = setOf("ThirdParty")
    private val thirdParty = mockPlugin("ThirdParty")

    private var disabledIds = emptySet<String>()
    private val registry = ServiceRegistry<TestService>("TestApi", plugin, defaultId = "minecraft") { disabledIds }
    private var preferredProviderId = ""

    // SUT
    private val providers = NamespacedProviders(registry, preferredProviderId = { preferredProviderId })

    private val minecraft = TestService(id = "minecraft", "level")
    private val skills = TestService(id = "skills", "power", "mining", "profession/fishing")
    private val jobs = TestService(id = "jobs", "total", "mining", "wood_cutter")

    init {
        registry.register(minecraft, plugin, Lowest)
        registry.register(skills, thirdParty, Normal)
        registry.register(jobs, thirdParty, Low)
    }

    @ParameterizedTest
    @CsvSource(
        "skills:mining,                 skills:mining",
        "Skills:MINING,                 skills:mining",
        "'  skills:mining ',            skills:mining",
        "skills:Profession/Fishing,     skills:profession/fishing",
        "Jobs:Wood Cutter,              jobs:wood_cutter",
        "Wood Cutter,                   jobs:wood_cutter",
        "power,                         skills:power",
        "Level,                         minecraft:level",
    )
    fun `when look up - should ignore spelling and find the only owner of a name`(value: String, expected: String) {
        val result = lookup(value)

        result.status shouldBe LookupStatus.FOUND
        result.ids shouldContainExactly listOf(Key.key(expected))
        result.foundId shouldBe Key.key(expected)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "skills:unknown", "minecraft:mining", "mimic:mining", "unknown", "fishing",
            "", "  ", ":mining", "skills:", "skills:mining:extra", "my plugin:mining",
        ],
    )
    fun `when look up unknown or malformed value - should not find it`(value: String) {
        val result = lookup(value)

        result.status shouldBe LookupStatus.NOT_FOUND
        result.ids.shouldBeEmpty()
        result.foundId.shouldBeNull()
    }

    @ParameterizedTest
    @ValueSource(strings = ["unknown:mining", "Unknown:Mining"])
    fun `when look up value of unknown namespace - should report no provider`(value: String) {
        val result = lookup(value)

        result.status shouldBe LookupStatus.NO_PROVIDER
        result.ids.shouldBeEmpty()
    }

    @Test
    fun `when look up name of several providers - should list them by priority and log nothing`() {
        val warnings = mutableListOf<String>()
        Log.init({ _, message, _ -> warnings += message.orEmpty() })

        val result = lookup("Mining")

        result.status shouldBe LookupStatus.AMBIGUOUS
        result.ids shouldContainExactly listOf(Key.key("skills:mining"), Key.key("jobs:mining"))
        result.foundId.shouldBeNull()
        warnings.shouldBeEmpty()
    }

    @ParameterizedTest
    @CsvSource(
        "jobs,      mining, FOUND,     jobs:mining",
        "skills,    mining, FOUND,     skills:mining",
        "minecraft, mining, AMBIGUOUS, ",
        "unknown,   mining, AMBIGUOUS, ",
        "jobs,      power,  FOUND,     skills:power",
    )
    fun `when look up name and preferred provider is set - should use it only to resolve ambiguity`(
        preferred: String,
        value: String,
        status: LookupStatus,
        expected: String?,
    ) {
        preferredProviderId = preferred

        val result = lookup(value)

        result.status shouldBe status
        result.foundId shouldBe expected?.let(Key::key)
    }

    @Test
    fun `when look up value before its plugin is enabled - should not find it until then`() {
        registry.register(TestService(id = "late", "combat"), mockPlugin("Late"), Normal)

        lookup("late:combat").status shouldBe LookupStatus.NOT_FOUND
        lookup("combat").status shouldBe LookupStatus.NOT_FOUND

        enabledPlugins += "Late"
        lookup("late:combat").foundId shouldBe Key.key("late:combat")
        lookup("combat").foundId shouldBe Key.key("late:combat")
    }

    @Test
    fun `when look up value of disabled provider - should not find it`() {
        disabledIds = setOf("skills")

        lookup("skills:power").status shouldBe LookupStatus.NOT_FOUND
        lookup("power").status shouldBe LookupStatus.NOT_FOUND
    }

    @Test
    fun `when look up value with namespace - should ask its provider only`() {
        lookup("skills:mining")

        minecraft.checks shouldBe 0
        jobs.checks shouldBe 0
    }

    @Test
    fun `when find unknown minecraft ID - should warn once with suggestions`() {
        val warnings = mutableListOf<String>()
        Log.init({ _, message, _ -> warnings += message.orEmpty() })

        repeat(2) {
            providers.find("minecraft:mining")
            providers.find("minecraft:level")
        }

        warnings.single() shouldContain "'minecraft:mining' doesn't exist"
        warnings.single() shouldContain "Did you mean 'skills:mining', 'jobs:mining'?"
    }

    @Test
    fun `when find unknown minecraft ID and API has vanilla values - should warn once only if other provider has it`() {
        val vanillaProviders = NamespacedProviders(registry, hasVanillaValues = true)
        val warnings = mutableListOf<String>()
        Log.init({ _, message, _ -> warnings += message.orEmpty() })

        repeat(2) {
            vanillaProviders.find("minecraft:mining")
            vanillaProviders.find("minecraft:unknown")
        }

        warnings.single() shouldContain "'minecraft:mining' doesn't exist"
        warnings.single() shouldContain "Did you mean 'skills:mining', 'jobs:mining'?"
    }

    private fun lookup(value: String): LookupResult = providers.lookup(value) { path -> service.has(path) }

    private fun NamespacedProviders<TestService>.find(id: String) =
        find(Key.key(id), valueName = "value") { path -> service.has(path) }

    private fun mockPlugin(name: String): Plugin = mockk {
        every { this@mockk.name } returns name
        every { isEnabled } answers { name in enabledPlugins }
    }

    private class TestService(override val id: String, vararg paths: String) : MimicService {
        private val paths = paths.toSet()
        var checks = 0

        fun has(path: String): Boolean {
            checks++
            return path in paths
        }
    }
}
