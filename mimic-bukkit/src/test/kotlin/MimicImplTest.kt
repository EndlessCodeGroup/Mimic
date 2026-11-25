// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // LevelSystem, ClassSystem and ItemsRegistry APIs are deprecated but still supported

package ru.endlesscode.mimic

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.ServicePriority.*
import ru.endlesscode.mimic.classes.BukkitClassSystem
import ru.endlesscode.mimic.classes.ClassProvider
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.impl.mimic.MimicLevels
import ru.endlesscode.mimic.impl.mimic.getPrimaryClassProviderId
import ru.endlesscode.mimic.impl.vanilla.MinecraftLevelProvider
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.items.ItemProvider
import ru.endlesscode.mimic.level.*
import kotlin.test.Test

internal class MimicImplTest : BukkitTestBase() {

    private val thirdParty = mockPlugin("ThirdParty")
    private val otherPlugin = mockPlugin("Other")

    private var preferredLevelProvider = ""
    private var preferredClassProvider = ""
    private val config: MimicConfig = mockk {
        every { levelProvider } answers { preferredLevelProvider }
        every { classProvider } answers { preferredClassProvider }
        every { disabledItemProviders } returns emptySet()
    }

    // SUT
    private val mimic = MimicImpl(plugin, config)

    init {
        mimic.registerLevelSystem(TestLevelSystemProvider("minecraft"), MimicApiLevel.CURRENT, plugin, Lowest)
        mimic.registerLevelProvider(MinecraftLevelProvider(), MimicApiLevel.CURRENT, plugin, Lowest)
        mimic.registerLevelSystem(TestLevelSystemProvider("custom"), MimicApiLevel.CURRENT, thirdParty, Normal)
        mimic.registerClassSystem(TestClassSystemProvider("permissions"), MimicApiLevel.CURRENT, plugin, Lowest)
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
        disabledPlugins += "ThirdParty"

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
        mimic.registerLevelSystem(TestLevelSystemProvider("builtin"), MimicApiLevel.CURRENT, plugin, Normal)
        mimic.registerLevelSystem(TestLevelSystemProvider("builtin"), MimicApiLevel.CURRENT, thirdParty, Normal)

        (mimic.levels as MimicLevels).providers.enabled.single { it.service.id == "builtin" }.plugin shouldBe thirdParty
    }

    @Test
    fun `when class system is registered - should provide it via classes as a single slot`() {
        mimic.registerClassSystem(TestClassSystemProvider("legacy"), MimicApiLevel.CURRENT, thirdParty, Normal)

        mimic.classes.getAllSlots().map { it.key().asString() } shouldContainExactly listOf("legacy:class")
        mimic.classes.getPrimaryClass(player)?.key()?.asString() shouldBe "legacy:mage"
        mimic.classes.lookupSlot("class").foundId?.asString() shouldBe "legacy:class"
        mimic.classes.lookupSlot("permissions:class").status shouldBe LookupStatus.NOT_FOUND
        // The old API can't list classes
        mimic.classes.lookupClass("legacy:mage").status shouldBe LookupStatus.NOT_FOUND
    }

    @Test
    fun `when class provider is registered after class system by another plugin - should replace it`() {
        mimic.registerClassSystem(TestClassSystemProvider("rpg"), MimicApiLevel.CURRENT, thirdParty, High)
        mimic.registerClassProvider(TestClassProvider("rpg"), MimicApiLevel.CURRENT, otherPlugin, Normal)

        mimic.classes.getAllSlots().map { it.key().asString() } shouldContainExactly listOf("rpg:native")
    }

    @Test
    fun `when class system is registered after class provider with the same ID - should not bridge it`() {
        mimic.registerClassProvider(TestClassProvider("rpg"), MimicApiLevel.CURRENT, thirdParty, Normal)
        mimic.registerClassSystem(TestClassSystemProvider("rpg"), MimicApiLevel.CURRENT, otherPlugin, High)

        mimic.classes.getAllSlots().map { it.key().asString() } shouldContainExactly listOf("rpg:native")
    }

    @Test
    fun `when class provider uses reserved ID - should not register it`() {
        mimic.registerClassProvider(TestClassProvider("permissions"), MimicApiLevel.CURRENT, thirdParty, Normal) shouldBe null
    }

    @Test
    fun `when there are no class plugins - should provide permissions only via class system`() {
        mimic.getClassSystemProvider().id shouldBe "permissions"

        mimic.classes.getAllSlots().shouldBeEmpty()
        mimic.classes.getAllClasses().shouldBeEmpty()
        mimic.classes.getClasses(player).shouldBeEmpty()
        mimic.classes.getPrimaryClass(player).shouldBeNull()
        mimic.getPrimaryClassProviderId().shouldBeNull()
    }

    @Test
    fun `when preferred class system is a bridged class system - should use it`() {
        mimic.registerClassSystem(TestClassSystemProvider("legacy"), MimicApiLevel.CURRENT, thirdParty, Normal)
        mimic.registerClassSystem(TestClassSystemProvider("other"), MimicApiLevel.CURRENT, otherPlugin, High)
        preferredClassProvider = "legacy"

        mimic.getClassSystemProvider().id shouldBe "legacy"
        mimic.classes.getPrimaryClass(player)?.key()?.asString() shouldBe "legacy:mage"
    }

    @Test
    fun `when preferred class system is permissions - should use it only as class system`() {
        mimic.registerClassSystem(TestClassSystemProvider("legacy"), MimicApiLevel.CURRENT, thirdParty, Normal)
        preferredClassProvider = "permissions"

        mimic.getClassSystemProvider().id shouldBe "permissions"
        mimic.classes.getPrimaryClass(player)?.key()?.asString() shouldBe "legacy:mage"
    }

    @Test
    fun `when preferred class system is a class provider - should use the class system with the highest priority`() {
        mimic.registerClassProvider(TestClassProvider("rpg"), MimicApiLevel.CURRENT, thirdParty, Normal)
        preferredClassProvider = "rpg"

        mimic.getClassSystemProvider().id shouldBe "permissions"
    }

    @Test
    fun `when items registry is registered - should provide its items via items`() {
        mimic.registerItemsRegistry(TestItemsRegistry("legacy"), MimicApiLevel.CURRENT, thirdParty, Normal)

        mimic.items.getAllItems().map { it.asString() } shouldContainExactly listOf("legacy:registry_item")
        mimic.itemsRegistry.knownIds shouldContainExactly listOf("legacy:registry_item")
        mimic.getAllItemsRegistries().keys shouldContainExactly listOf("legacy")
    }

    // https://github.com/EndlessCodeGroup/Mimic/issues/85
    @Test
    fun `when items registry is registered - should keep its own way to compare items`() {
        val registry = mimic.registerItemsRegistry(TestItemsRegistry("legacy"), MimicApiLevel.CURRENT, thirdParty, Normal)

        registry?.isSameItem(ItemStack(Material.STONE), "any").shouldBeTrue()
    }

    @Test
    fun `when item provider is registered after items registry by another plugin - should replace it`() {
        mimic.registerItemsRegistry(TestItemsRegistry("custom"), MimicApiLevel.CURRENT, thirdParty, High)
        mimic.registerItemProvider(TestItemProvider("custom"), MimicApiLevel.CURRENT, otherPlugin, Normal)

        mimic.items.getAllItems().map { it.asString() } shouldContainExactly listOf("custom:provider_item")
    }

    @Test
    fun `when items registry is registered after item provider with the same ID - should not bridge it`() {
        mimic.registerItemProvider(TestItemProvider("custom"), MimicApiLevel.CURRENT, thirdParty, Normal)
        mimic.registerItemsRegistry(TestItemsRegistry("custom"), MimicApiLevel.CURRENT, otherPlugin, High)

        mimic.items.getAllItems().map { it.asString() } shouldContainExactly listOf("custom:provider_item")
    }

    @Test
    fun `when items registry of ItemsAdder is registered - should not bridge it`() {
        mimic.registerItemsRegistry(TestItemsRegistry("ia"), MimicApiLevel.CURRENT, thirdParty, Normal)

        mimic.items.getAllItems().shouldBeEmpty()
        mimic.getAllItemsRegistries().keys shouldContainExactly listOf("ia")
    }

    @Test
    fun `when plugin is disabled - should unregister its items registries and item providers`() {
        mimic.registerItemsRegistry(TestItemsRegistry("legacy"), MimicApiLevel.CURRENT, thirdParty, Normal)
        mimic.registerItemProvider(TestItemProvider("custom"), MimicApiLevel.CURRENT, thirdParty, Normal)

        mimic.onPluginDisable(PluginDisableEvent(thirdParty))

        mimic.items.getAllItems().shouldBeEmpty()
    }

    private fun List<LevelTrack>.keys() = map { it.key().asString() }

    private fun Levels.trackName(id: String) = getAllTracks().single { it.key().asString() == id }.name()

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

    private class TestClassProvider(override val id: String) : ClassProvider {
        override val allSlots: Collection<ClassProvider.Slot> = listOf(ClassProvider.Slot("native", Component.empty()))
        override val allClasses: Collection<ClassProvider.ClassDefinition> = emptyList()
        override fun getClasses(player: Player): Map<String, ClassProvider.ClassDefinition> = emptyMap()
        override fun getPrimarySlot(player: Player): ClassProvider.Slot? = null
    }

    private class TestItemProvider(override val id: String) : ItemProvider {
        override val allItems: Collection<String> = listOf("provider_item")
        override fun getItemId(item: ItemStack): String? = null
        override fun createItem(itemId: String, amount: Int, player: Player?, payload: String?): ItemStack? = null
    }

    private class TestItemsRegistry(override val id: String) : BukkitItemsRegistry {
        override val knownIds: Collection<String> = listOf("registry_item")
        override fun isSameItem(item: ItemStack, itemId: String): Boolean = true
        override fun isItemExists(itemId: String): Boolean = itemId in knownIds
        override fun getItemId(item: ItemStack): String? = null
        override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? = null
    }

    private class TestClassSystemProvider(override val id: String) : BukkitClassSystem.Provider {
        override fun getSystem(player: Player): BukkitClassSystem = mockk {
            every { primaryClass } returns "Mage"
        }
    }
}
