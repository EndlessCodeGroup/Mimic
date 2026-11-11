package ru.endlesscode.mimic

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.ServicePriority.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.Test

internal class ServiceRegistryTest : BukkitTestBase() {

    private val thirdParty = mockPlugin("ThirdParty")
    private val anotherThirdParty = mockPlugin("AnotherThirdParty")

    // SUT
    private val registry = ServiceRegistry<TestService>("TestApi", plugin, reservedIds = setOf("default"))

    @Test
    fun `when register services - should sort them by priority keeping registration order`() {
        registry.register(TestService("first-normal"), thirdParty, Normal)
        registry.register(TestService("lowest"), thirdParty, Lowest)
        registry.register(TestService("highest"), thirdParty, Highest)
        registry.register(TestService("second-normal"), thirdParty, Normal)

        registry.registeredIds() shouldContainExactly listOf("highest", "first-normal", "second-normal", "lowest")
    }

    @Test
    fun `when register default implementation - should place it after others regardless of priority`() {
        registry.register(TestService("default"), plugin, Highest)
        registry.register(TestService("built-in"), plugin, Lowest)
        registry.register(TestService("third-party"), thirdParty, Lowest)

        registry.registeredIds() shouldContainExactly listOf("built-in", "third-party", "default")
    }

    @Test
    fun `when third party registers built-in ID - should replace built-in regardless of priority`() {
        registry.register(TestService("id"), plugin, Highest)

        registry.register(TestService("id"), thirdParty, Lowest).shouldBeTrue()
        registry.registeredPlugins() shouldContainExactly listOf(thirdParty)
    }

    @Test
    fun `when built-in registers ID registered by third party - should skip built-in`() {
        registry.register(TestService("id"), thirdParty, Lowest)

        registry.register(TestService("id"), plugin, Highest).shouldBeFalse()
        registry.registeredPlugins() shouldContainExactly listOf(thirdParty)
    }

    @ParameterizedTest
    @CsvSource(
        "Low,    High,   true",
        "High,   Low,    false",
        "Normal, Normal, false",
    )
    fun `when two third parties register the same ID - should keep the one with higher priority`(
        firstPriority: ServicePriority,
        secondPriority: ServicePriority,
        shouldReplace: Boolean,
    ) {
        registry.register(TestService("id"), thirdParty, firstPriority)

        registry.register(TestService("id"), anotherThirdParty, secondPriority) shouldBe shouldReplace
        registry.registeredPlugins() shouldContainExactly listOf(if (shouldReplace) anotherThirdParty else thirdParty)
    }

    @Test
    fun `when plugin registers its ID again - should replace the previous service keeping its place`() {
        registry.register(TestService("first"), thirdParty, Normal)
        registry.register(TestService("second"), anotherThirdParty, Normal)
        val newService = TestService("first")

        registry.register(newService, thirdParty, Normal).shouldBeTrue()
        registry.registeredIds() shouldContainExactly listOf("first", "second")
        registry.entries.first().service shouldBeSameInstanceAs newService
    }

    @Test
    fun `when third party registers reserved ID - should not register it`() {
        registry.register(TestService("default"), thirdParty, Highest).shouldBeFalse()
        registry.registeredIds() shouldContainExactly emptyList()
    }

    @Test
    fun `when built-in registers reserved ID - should register it`() {
        registry.register(TestService("default"), plugin, Lowest).shouldBeTrue()
        registry.registeredIds() shouldContainExactly listOf("default")
    }

    @Test
    fun `when third party registers reserved ID registered by built-in - should keep built-in`() {
        registry.register(TestService("default"), plugin, Lowest)

        registry.register(TestService("default"), thirdParty, Highest).shouldBeFalse()
        registry.registeredPlugins() shouldContainExactly listOf(plugin)
    }

    @Test
    fun `when unregister plugin services - should remove only services of this plugin`() {
        registry.register(TestService("built-in"), plugin, Normal)
        registry.register(TestService("third-party"), thirdParty, Normal)
        registry.register(TestService("another-third-party"), anotherThirdParty, Normal)

        registry.unregisterAll(thirdParty)

        registry.registeredIds() shouldContainExactly listOf("built-in", "another-third-party")
    }

    private fun ServiceRegistry<TestService>.registeredIds() = entries.map { it.service.id }
    private fun ServiceRegistry<TestService>.registeredPlugins() = entries.map { it.plugin }

    private fun mockPlugin(name: String): Plugin = mockk {
        every { this@mockk.name } returns name
    }

    private class TestService(override val id: String) : MimicService
}
