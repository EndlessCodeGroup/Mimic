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
import io.mockk.mockk
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority.Lowest
import org.bukkit.plugin.ServicePriority.Normal
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.LookupStatus
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.resources.Resource
import ru.endlesscode.mimic.resources.ResourceProvider
import ru.endlesscode.mimic.resources.ResourceProvider.ResourceDefinition
import ru.endlesscode.mimic.resources.ResourceState
import kotlin.math.floor
import kotlin.test.Test

internal class MimicResourcesTest : BukkitTestBase() {

    private val thirdParty: Plugin = mockk {
        every { name } returns "ThirdParty"
        every { isEnabled } returns true
        every { pluginMeta } returns mockk { every { authors } returns listOf("Author") }
    }

    private val resourceProviders = ServiceRegistry<ResourceProvider>("ResourceProvider", plugin, defaultId = "minecraft")
    private var preferredProviderId = ""

    // SUT
    private val resources = MimicResources(resourceProviders, preferredProviderId = { preferredProviderId })

    private val minecraft = TestResourceProvider(id = "minecraft", "food").apply { primaryResource = null }
    private val rpg = TestResourceProvider(id = "rpg", "mana", "stamina")

    init {
        resourceProviders.register(minecraft, plugin, Lowest)
        resourceProviders.register(rpg, thirdParty, Normal)
    }

    @Test
    fun `when get all resources - should return namespaced resources of all providers`() {
        resources.getAllResources().keys() shouldContainExactly listOf("rpg:mana", "rpg:stamina", "minecraft:food")
    }

    @Test
    fun `when get all resources - should skip invalid and duplicated paths`() {
        val provider = TestResourceProvider(id = "broken", "valid", "Invalid", "with space", "", "ns:path", "valid")
        resourceProviders.register(provider, thirdParty, Normal)

        val brokenResources = resources.getAllResources().filter { it.key().namespace() == "broken" }
        brokenResources.keys() shouldHaveSingleElement "broken:valid"
    }

    @Test
    fun `when provider throws - should skip it`() {
        rpg.failure = IllegalStateException("Test exception")

        resources.getAllResources().keys() shouldHaveSingleElement "minecraft:food"
        resources.getState(player, MANA).shouldBeNull()
        resources.take(player, MANA, 1.0).shouldBeFalse()
        resources.give(player, MANA, 1.0).shouldBeFalse()
        resources.set(player, MANA, 1.0).shouldBeFalse()
    }

    @Test
    fun `when provider returns null resource - should skip the provider`() {
        @Suppress("UNCHECKED_CAST") // Simulates a provider implemented in Java
        val javaResources = listOf<ResourceDefinition?>(null) as List<ResourceDefinition>
        val javaProvider = object : ResourceProvider {
            override val id: String = "java"
            override val allResources: Collection<ResourceDefinition> = javaResources
            override fun getPrimaryResource(player: Player): ResourceDefinition? = null
            override fun getState(player: Player, resourceId: String): ResourceState? = null
        }
        resourceProviders.register(javaProvider, thirdParty, Normal)

        resources.getAllResources().keys().filter { it.startsWith("java:") }.shouldBeEmpty()
    }

    @Test
    fun `when preferred provider is not set - should skip providers without primary resource`() {
        resourceProviders.register(TestResourceProvider("first", "rage").apply { primaryResource = null }, thirdParty, Normal)

        resources.getPrimaryResource(player)?.key()?.asString() shouldBe "rpg:mana"
    }

    @Test
    fun `when only default provider is registered - should return no primary resource`() {
        resourceProviders.unregisterAll(thirdParty)

        resources.getPrimaryResource(player).shouldBeNull()
    }

    @Test
    fun `when preferred provider is set - should use its primary resource`() {
        resourceProviders.register(TestResourceProvider("other", "energy"), thirdParty, Lowest)
        preferredProviderId = "other"

        resources.getPrimaryResource(player)?.key()?.asString() shouldBe "other:energy"
    }

    @Test
    fun `when preferred provider has no primary resource - should return null`() {
        preferredProviderId = "minecraft"

        resources.getPrimaryResource(player).shouldBeNull()
    }

    @Test
    fun `when compare resources - should compare keys`() {
        resources.getAllResources().first() shouldBe resources.getPrimaryResource(player)
    }

    @Test
    fun `when player is offline - should not call providers`() {
        every { player.isOnline } returns false

        resources.getPrimaryResource(player).shouldBeNull()
        resources.getState(player, MANA).shouldBeNull()
        resources.has(player, MANA, 1.0).shouldBeFalse()
        resources.take(player, MANA, 1.0).shouldBeFalse()
        resources.give(player, MANA, 1.0).shouldBeFalse()
        resources.set(player, MANA, 1.0).shouldBeFalse()
        rpg.reads shouldBe 0
        rpg.writes.shouldBeEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = ["rpg:unknown", "unknown:mana", "minecraft:mana"])
    fun `when resource is unknown - should return nothing`(resource: String) {
        resources.getState(player, Key.key(resource)).shouldBeNull()
        resources.has(player, Key.key(resource), 0.0).shouldBeFalse()
        resources.take(player, Key.key(resource), 0.0).shouldBeFalse()
    }

    @Test
    fun `when get state - should use provider selected by namespace`() {
        minecraft.amounts["food"] = 7.0

        resources.getState(player, Key.key("minecraft:food")) shouldBe ResourceState.of(7.0, 20.0)
        resources.getState(player, MANA) shouldBe ResourceState.of(10.0, 20.0)
    }

    @ParameterizedTest
    @CsvSource("0.0, true", "9.5, true", "10.0, true", "10.5, false")
    fun `when check amount - should check that player has at least the given amount`(amount: Double, expected: Boolean) {
        resources.has(player, MANA, amount) shouldBe expected
    }

    @Test
    fun `when take - should take the amount`() {
        resources.take(player, MANA, 4.0).shouldBeTrue()

        rpg.amounts["mana"] shouldBe 6.0
    }

    @Test
    fun `when take more than player has - should take nothing`() {
        resources.take(player, MANA, 10.5).shouldBeFalse()

        rpg.writes.shouldBeEmpty()
    }

    @Test
    fun `when take nothing - should not call provider`() {
        resources.take(player, MANA, 0.0).shouldBeTrue()

        rpg.writes.shouldBeEmpty()
    }

    @Test
    fun `when state can't be read after take - should return true`() {
        rpg.changeAmount = { _, new ->
            rpg.failure = IllegalStateException("Test exception")
            new
        }

        resources.take(player, MANA, 4.0).shouldBeTrue()
    }

    @Test
    fun `when take from amount above max - should not lower it to max`() {
        rpg.amounts["mana"] = 25.0

        resources.take(player, MANA, 1.0).shouldBeTrue()
        rpg.amounts["mana"] shouldBe 24.0
    }

    @Test
    fun `when take is cancelled by plugin - should return false`() {
        rpg.changeAmount = { current, _ -> current }

        resources.take(player, MANA, 4.0).shouldBeFalse()
    }

    @Test
    fun `when plugin takes less than requested - should return true`() {
        rpg.changeAmount = { current, new -> current - (current - new) / 2 }

        resources.take(player, MANA, 4.0).shouldBeTrue()
        rpg.amounts["mana"] shouldBe 8.0
    }

    @Test
    fun `when plugin stores whole numbers - should take more and give less`() {
        rpg.changeAmount = { _, new -> floor(new) }

        resources.take(player, MANA, 0.5).shouldBeTrue()
        rpg.amounts["mana"] shouldBe 9.0

        resources.give(player, MANA, 1.5).shouldBeTrue()
        rpg.amounts["mana"] shouldBe 10.0
    }

    @ParameterizedTest
    @CsvSource("0.0, 10.0", "4.5, 14.5", "10.0, 20.0", "100.0, 20.0")
    fun `when give - should not exceed max`(amount: Double, expected: Double) {
        resources.give(player, MANA, amount).shouldBeTrue()

        rpg.amounts["mana"] shouldBe expected
    }

    @Test
    fun `when give and amount is above max - should not lower it`() {
        rpg.amounts["mana"] = 25.0

        resources.give(player, MANA, 1.0).shouldBeTrue()
        rpg.writes.shouldBeEmpty()
    }

    @ParameterizedTest
    @CsvSource("-5.0, 0.0", "0.0, 0.0", "12.5, 12.5", "100.0, 20.0")
    fun `when set - should keep amount between zero and max`(amount: Double, expected: Double) {
        resources.set(player, MANA, amount).shouldBeTrue()

        rpg.amounts["mana"] shouldBe expected
    }

    @ParameterizedTest
    @ValueSource(doubles = [-1.0, Double.NaN, Double.POSITIVE_INFINITY])
    fun `when amount is invalid - should throw`(amount: Double) {
        shouldThrow<IllegalArgumentException> { resources.has(player, MANA, amount) }
        shouldThrow<IllegalArgumentException> { resources.take(player, MANA, amount) }
        shouldThrow<IllegalArgumentException> { resources.give(player, MANA, amount) }
        if (!amount.isFinite()) shouldThrow<IllegalArgumentException> { resources.set(player, MANA, amount) }
    }

    @Test
    fun `when provider does not support writes - should return false`() {
        val readOnly = object : ResourceProvider {
            override val id: String = "readonly"
            override val allResources: Collection<ResourceDefinition> = emptyList()
            override fun getPrimaryResource(player: Player): ResourceDefinition? = null
            override fun getState(player: Player, resourceId: String): ResourceState = ResourceState.of(10.0, 20.0)
        }
        resourceProviders.register(readOnly, thirdParty, Normal)
        val energy = Key.key("readonly:energy")

        resources.has(player, energy, 5.0).shouldBeTrue()
        resources.take(player, energy, 5.0).shouldBeFalse()
        resources.give(player, energy, 5.0).shouldBeFalse()
        resources.set(player, energy, 5.0).shouldBeFalse()
    }

    @ParameterizedTest
    @CsvSource(
        "RPG:Stamina,   ,      FOUND,       rpg:stamina",
        "food,          ,      FOUND,       minecraft:food",
        "mana,          ,      AMBIGUOUS,   ",
        "mana,          other, FOUND,       other:mana",
        "rpg:unknown,   ,      NOT_FOUND,   ",
    )
    fun `when look up resource - should find it among resources of providers`(
        value: String,
        preferred: String?,
        status: LookupStatus,
        expected: String?,
    ) {
        resourceProviders.register(TestResourceProvider(id = "other", "mana"), thirdParty, Lowest)
        preferredProviderId = preferred.orEmpty()

        val result = resources.lookupResource(value)

        result.status shouldBe status
        result.foundId shouldBe expected?.let(Key::key)
    }

    private fun List<Resource>.keys() = map { it.key().asString() }

    /** Keeps amounts in memory, every resource starts with 10 of 20. */
    private class TestResourceProvider(
        override val id: String,
        vararg paths: String,
    ) : ResourceProvider {
        var failure: Throwable? = null
        var primaryResource: String? = paths.first()
        val amounts = paths.associateWithTo(mutableMapOf()) { 10.0 }
        val writes = mutableListOf<String>()
        var reads = 0

        /** What the plugin does with a requested amount: returns the amount to store. */
        var changeAmount: (current: Double, new: Double) -> Double = { _, new -> new }

        override val allResources: Collection<ResourceDefinition> = paths.map(::definition)
            get() = checked { field }

        override fun getPrimaryResource(player: Player): ResourceDefinition? =
            checked { reads++; primaryResource?.let(::definition) }

        override fun getState(player: Player, resourceId: String): ResourceState? =
            checked { reads++; amounts[resourceId]?.let { ResourceState.of(it, 20.0) } }

        override fun setAmount(player: Player, resourceId: String, amount: Double): Boolean = checked {
            writes += "set $resourceId $amount"
            amounts[resourceId] = changeAmount(amounts.getValue(resourceId), amount)
            true
        }

        private fun definition(path: String) = ResourceDefinition(path, Component.text("Name of $path"))

        private inline fun <T> checked(block: () -> T): T {
            failure?.let { throw it }
            return block()
        }
    }

    private companion object {
        val MANA = Key.key("rpg:mana")
    }
}
