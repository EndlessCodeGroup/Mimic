// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mimic

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.plugin.ServicePriority.Lowest
import org.bukkit.plugin.ServicePriority.Normal
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.LookupStatus
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.classes.CharacterClass
import ru.endlesscode.mimic.classes.ClassProvider
import ru.endlesscode.mimic.internal.Log
import kotlin.test.Test

internal class MimicClassesTest : BukkitTestBase() {

    private val thirdParty = mockPlugin("ThirdParty")

    private val classProviders = ServiceRegistry<ClassProvider>("ClassProvider", plugin, defaultId = "permissions")
    private var preferredProviderId = ""

    // SUT
    private val classes = MimicClasses(classProviders, preferredProviderId = { preferredProviderId })

    private val legacy = TestClassProvider(
        id = "legacy",
        slots = listOf("class"),
        classes = listOf(definition("mage")),
        playerClasses = mapOf("class" to "mage"),
    )
    private val rpg = TestClassProvider(
        id = "rpg",
        slots = listOf("class", "race"),
        classes = listOf(
            definition("warrior", levelTrack = "class"),
            definition("paladin", "warrior"),
            definition("templar", "paladin"),
            definition("elf"),
        ),
        playerClasses = mapOf("class" to "paladin", "race" to "elf"),
    )

    init {
        classProviders.register(legacy, plugin, Lowest)
        classProviders.register(rpg, thirdParty, Normal)
    }

    @Test
    fun `when get all slots and classes - should return namespaced values of all providers`() {
        classes.getAllSlots().map { it.key().asString() } shouldContainExactly listOf(
            "rpg:class",
            "rpg:race",
            "legacy:class",
        )
        classes.getAllClasses().keys() shouldContainExactly listOf(
            "rpg:warrior",
            "rpg:paladin",
            "rpg:templar",
            "rpg:elf",
            "legacy:mage",
        )
    }

    @Test
    fun `when get all classes - should skip invalid and duplicated IDs`() {
        val provider = TestClassProvider(
            id = "broken",
            classes = listOf(definition("valid"), definition("Invalid"), definition(""), definition("valid")),
        )
        classProviders.register(provider, thirdParty, Normal)

        classes.getAllClasses().keys().filter { it.startsWith("broken:") } shouldContainExactly listOf("broken:valid")
    }

    @Test
    fun `when get all slots - should skip invalid and duplicated IDs`() {
        val provider = TestClassProvider(id = "broken", slots = listOf("valid", "Invalid", "valid"))
        classProviders.register(provider, thirdParty, Normal)

        classes.getAllSlots().map { it.key().asString() }.filter { it.startsWith("broken:") } shouldContainExactly
            listOf("broken:valid")
    }

    @Test
    fun `when get player classes - should namespace slots and classes`() {
        classes.getClasses(player).map { (slot, playerClass) -> slot.asString() to playerClass.key().asString() }
            .shouldContainExactly(
                "rpg:class" to "rpg:paladin",
                "rpg:race" to "rpg:elf",
                "legacy:class" to "legacy:mage",
            )
    }

    @Test
    fun `when get player classes - should skip invalid slots and classes`() {
        rpg.playerClasses = mapOf("Invalid" to "paladin", "race" to "Invalid", "class" to "warrior")

        classes.getClasses(player).filterKeys { it.namespace() == "rpg" }.values.keys() shouldContainExactly
            listOf("rpg:warrior")
    }

    @Test
    fun `when get class - should namespace its level track`() {
        rpg.playerClasses = mapOf("class" to "warrior")

        classes.getClasses(player).getValue(Key.key("rpg:class")).levelTrack() shouldBe Key.key("rpg:class")
        classes.getAllClasses().single { it.key() == Key.key("rpg:paladin") }.levelTrack().shouldBeNull()
    }

    @Test
    fun `when level track is invalid - should return class without level track`() {
        rpg.classes = listOf(definition("warrior", levelTrack = "Invalid"))

        classes.getAllClasses().single { it.key() == Key.key("rpg:warrior") }.levelTrack().shouldBeNull()
    }

    @Test
    fun `when compare classes - should compare keys`() {
        classes.getPrimaryClass(player) shouldBe classes.getAllClasses().single { it.key() == Key.key("rpg:paladin") }
    }

    @Test
    fun `when preferred provider is not set - should use primary class of the provider with the highest priority`() {
        classes.getPrimaryClass(player)?.key()?.asString() shouldBe "rpg:paladin"
    }

    @Test
    fun `when preferred provider is set - should use its primary class`() {
        preferredProviderId = "legacy"
        classes.getPrimaryClass(player)?.key()?.asString() shouldBe "legacy:mage"
    }

    @Test
    fun `when preferred provider has no primary class - should return null`() {
        preferredProviderId = "rpg"
        rpg.playerClasses = mapOf("race" to "elf")
        classes.getPrimaryClass(player).shouldBeNull()
    }

    @Test
    fun `when preferred provider is not set and the first one has no primary class - should use the next one`() {
        rpg.primarySlot = null
        classes.getPrimaryClass(player)?.key()?.asString() shouldBe "legacy:mage"
    }

    @Test
    fun `when preferred provider is unknown - should use primary class of other providers`() {
        preferredProviderId = "unknown"
        classes.getPrimaryClass(player)?.key()?.asString() shouldBe "rpg:paladin"
    }

    @ParameterizedTest
    @CsvSource(
        "paladin,  rpg:paladin,     true",
        "paladin,  rpg:warrior,     true",
        "templar,  rpg:warrior,     true",
        "warrior,  rpg:paladin,     false",
        "paladin,  rpg:templar,     false",
        "paladin,  rpg:elf,         false",
        "paladin,  legacy:mage,     true",
        "paladin,  unknown:paladin, false",
    )
    fun `when check class - should check the class and its subclasses`(playerClass: String, classId: String, expected: Boolean) {
        rpg.playerClasses = mapOf("class" to playerClass)
        classes.hasClass(player, Key.key(classId)) shouldBe expected
    }

    @Test
    fun `when player class is not in all classes - should use its own parents`() {
        rpg.classes = emptyList()
        rpg.playerClasses = mapOf("class" to "paladin")
        rpg.extraClasses = mapOf("paladin" to definition("paladin", "warrior"))

        classes.hasClass(player, Key.key("rpg:warrior")).shouldBeTrue()
    }

    @Test
    fun `when class is defined twice - should use the first definition`() {
        rpg.classes = listOf(definition("paladin", "warrior"), definition("templar", "paladin"), definition("paladin"))
        rpg.playerClasses = mapOf("class" to "templar")

        classes.hasClass(player, Key.key("rpg:warrior")).shouldBeTrue()
    }

    @Test
    fun `when player class is in invalid slot - should ignore it`() {
        rpg.playerClasses = mapOf("Invalid" to "paladin")

        classes.hasClass(player, Key.key("rpg:paladin")).shouldBeFalse()
        classes.hasClass(player, Key.key("rpg:warrior")).shouldBeFalse()
    }

    @Test
    fun `when primary slot is invalid - should use the next provider`() {
        rpg.primarySlot = "Invalid"
        rpg.playerClasses = mapOf("Invalid" to "paladin", "race" to "elf")

        classes.getPrimaryClass(player)?.key()?.asString() shouldBe "legacy:mage"
    }

    @Test
    fun `when parents have a cycle - should not loop`() {
        rpg.classes = listOf(definition("a", "b"), definition("b", "a"))
        rpg.playerClasses = mapOf("class" to "a")

        classes.hasClass(player, Key.key("rpg:b")).shouldBeTrue()
        classes.hasClass(player, Key.key("rpg:c")).shouldBeFalse()
    }

    @Test
    fun `when set class - should pass class and slot to the provider`() {
        classes.setClass(player, Key.key("rpg:warrior")).shouldBeTrue()
        classes.setClass(player, Key.key("rpg:elf"), Key.key("rpg:race")).shouldBeTrue()
        rpg.writes shouldContainExactly listOf("warrior in null", "elf in race")
    }

    @Test
    fun `when set class in a slot of another provider - should not call providers`() {
        classes.setClass(player, Key.key("rpg:warrior"), Key.key("legacy:class")).shouldBeFalse()
        rpg.writes.shouldBeEmpty()
        legacy.writes.shouldBeEmpty()
    }

    @Test
    fun `when set class in a slot without namespace - should warn once with suggestions`() {
        val warnings = mutableListOf<String>()
        Log.init({ _, message, _ -> warnings += message.orEmpty() })

        repeat(2) { classes.setClass(player, Key.key("rpg:elf"), Key.key("race")).shouldBeFalse() }

        warnings.single() shouldContain "Slot 'minecraft:race' doesn't exist"
        warnings.single() shouldContain "Did you mean 'rpg:race'?"
        rpg.writes.shouldBeEmpty()
    }

    @Test
    fun `when set class without namespace in a slot - should warn once with suggestions`() {
        val warnings = mutableListOf<String>()
        Log.init({ _, message, _ -> warnings += message.orEmpty() })

        repeat(2) { classes.setClass(player, Key.key("elf"), Key.key("rpg:race")).shouldBeFalse() }

        warnings.single() shouldContain "Class 'minecraft:elf' doesn't exist"
        warnings.single() shouldContain "Did you mean 'rpg:elf'?"
        rpg.writes.shouldBeEmpty()
    }

    @Test
    fun `when provider does not support writes - should return false`() {
        val readOnly = object : ClassProvider {
            override val id: String = "readonly"
            override val allSlots: Collection<ClassProvider.Slot> = emptyList()
            override val allClasses: Collection<ClassProvider.ClassDefinition> = emptyList()
            override fun getClasses(player: Player): Map<String, ClassProvider.ClassDefinition> = emptyMap()
            override fun getPrimarySlot(player: Player): ClassProvider.Slot? = null
        }
        classProviders.register(readOnly, thirdParty, Normal)

        classes.setClass(player, Key.key("readonly:warrior")).shouldBeFalse()
    }

    @Test
    fun `when provider throws - should skip it`() {
        rpg.failure = IllegalStateException("Test exception")

        classes.getAllClasses().keys() shouldContainExactly listOf("legacy:mage")
        classes.getClasses(player).keys.map { it.asString() } shouldContainExactly listOf("legacy:class")
        classes.getPrimaryClass(player)?.key()?.asString() shouldBe "legacy:mage"
        classes.hasClass(player, Key.key("rpg:warrior")).shouldBeFalse()
        classes.setClass(player, Key.key("rpg:warrior")).shouldBeFalse()
    }

    @Test
    fun `when player is offline - should not call providers`() {
        every { player.isOnline } returns false

        classes.getClasses(player).shouldBeEmpty()
        classes.getPrimaryClass(player).shouldBeNull()
        classes.hasClass(player, Key.key("rpg:paladin")).shouldBeFalse()
        classes.setClass(player, Key.key("rpg:warrior")).shouldBeFalse()
        rpg.writes.shouldBeEmpty()
    }

    @Test
    fun `when set class of non-minecraft namespace - should not read all classes`() {
        classes.setClass(player, Key.key("rpg:warrior"))
        rpg.allClassesReads shouldBe 0
    }

    @ParameterizedTest
    @CsvSource(
        "RPG:Race,           ,       FOUND,       rpg:race",
        "race,               ,       FOUND,       rpg:race",
        "class,              ,       AMBIGUOUS,   ",
        "class,              legacy, FOUND,       legacy:class",
        "rpg:warrior,        ,       NOT_FOUND,   ",
        "permissions:class,  ,       NOT_FOUND,   ",
    )
    fun `when look up slot - should find it among slots of providers`(
        value: String,
        preferred: String?,
        status: LookupStatus,
        expected: String?,
    ) {
        preferredProviderId = preferred.orEmpty()

        val result = classes.lookupSlot(value)

        result.status shouldBe status
        result.foundId shouldBe expected?.let(Key::key)
    }

    @ParameterizedTest
    @CsvSource(
        "RPG:Paladin,        ,       FOUND,       rpg:paladin",
        "elf,                ,       FOUND,       rpg:elf",
        "mage,               ,       AMBIGUOUS,   ",
        "mage,               legacy, FOUND,       legacy:mage",
        "class,              ,       NOT_FOUND,   ",
        "permissions:mage,   ,       NOT_FOUND,   ",
    )
    fun `when look up class - should find it among classes of providers`(
        value: String,
        preferred: String?,
        status: LookupStatus,
        expected: String?,
    ) {
        classProviders.register(TestClassProvider(id = "other", classes = listOf(definition("mage"))), thirdParty, Lowest)
        preferredProviderId = preferred.orEmpty()

        val result = classes.lookupClass(value)

        result.status shouldBe status
        result.foundId shouldBe expected?.let(Key::key)
    }

    private fun Collection<CharacterClass>.keys() = map { it.key().asString() }

    private class TestClassProvider(
        override val id: String,
        slots: List<String> = emptyList(),
        var classes: List<ClassProvider.ClassDefinition> = emptyList(),
        var playerClasses: Map<String, String> = emptyMap(),
    ) : ClassProvider {
        var failure: Throwable? = null
        var primarySlot: String? = slots.firstOrNull()
        var extraClasses: Map<String, ClassProvider.ClassDefinition> = emptyMap()
        val writes = mutableListOf<String>()
        var allClassesReads = 0

        override val allSlots: Collection<ClassProvider.Slot> = slots.map(::slot)
            get() = checked { field }

        override val allClasses: Collection<ClassProvider.ClassDefinition>
            get() = checked { allClassesReads++; classes }

        override fun getClasses(player: Player): Map<String, ClassProvider.ClassDefinition> = checked {
            playerClasses.mapValues { (_, classId) ->
                classes.find { it.id == classId } ?: extraClasses[classId] ?: definition(classId)
            }
        }

        override fun getPrimarySlot(player: Player): ClassProvider.Slot? = checked { primarySlot?.let(::slot) }

        override fun setClass(player: Player, classId: String, slotId: String?): Boolean =
            checked { writes += "$classId in $slotId"; true }

        private fun slot(id: String) = ClassProvider.Slot(id, Component.text("Name of $id"))

        private inline fun <T> checked(block: () -> T): T {
            failure?.let { throw it }
            return block()
        }
    }

    private companion object {
        fun definition(id: String, vararg parents: String, levelTrack: String? = null) =
            ClassProvider.ClassDefinition(id, Component.text("Name of $id"), parents.toSet(), levelTrack)
    }
}
