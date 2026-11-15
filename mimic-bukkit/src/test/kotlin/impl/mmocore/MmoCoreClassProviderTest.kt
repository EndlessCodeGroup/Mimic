// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mmocore

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent
import net.Indyuce.mmocore.api.player.PlayerData
import net.Indyuce.mmocore.api.player.profess.PlayerClass
import net.Indyuce.mmocore.api.player.profess.Subclass
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

internal class MmoCoreClassProviderTest : BukkitTestBase() {

    private val human = mockClass("HUMAN")
    private val paladin = mockClass("PALADIN")
    private val warrior = mockClass("WARRIOR", subclasses = listOf(paladin))
    private val arcaneMage = mockClass("ARCANE_MAGE", name = "§5Arcane Mage")

    private var playerClass = paladin
    private var isLoaded = true
    private var isEventCancelled = false
    private val events = mutableListOf<PlayerChangeClassEvent>()
    private val selectedClasses = mutableListOf<PlayerClass>()

    private val data: PlayerData = mockk {
        every { player } returns this@MmoCoreClassProviderTest.player
        every { profess } answers { playerClass }
    }

    // SUT
    private val provider = MmoCoreClassProvider(
        mockk {
            every { getLoadedData(player) } answers { if (isLoaded) data else null }
            every { classes } returns listOf(human, warrior, paladin, arcaneMage)
            every { defaultClass } returns human
            every { callEvent(any()) } answers {
                val event = firstArg<PlayerChangeClassEvent>()
                event.isCancelled = isEventCancelled
                events += event
            }
            every { selectClass(data, any()) } answers { selectedClasses += secondArg<PlayerClass>() }
        },
    )

    @Test
    fun `when get all classes - should convert IDs, skip the default class and invert subclasses`() {
        val classes = provider.allClasses

        classes.map { it.id } shouldContainExactly listOf("warrior", "paladin", "arcane_mage")
        classes.associate { it.id to it.parents } shouldBe mapOf(
            "warrior" to emptySet(),
            "paladin" to setOf("warrior"),
            "arcane_mage" to emptySet(),
        )
        classes.map { it.levelTrack }.toSet() shouldBe setOf("level")
    }

    @Test
    fun `when get classes - should put the class with its parents into the class slot`() {
        val classes = provider.getClasses(player)

        classes.keys shouldContainExactly setOf("class")
        classes.getValue("class").id shouldBe "paladin"
        classes.getValue("class").parents shouldBe setOf("warrior")
    }

    @Test
    fun `when player has the default class - should return empty slot`() {
        playerClass = human
        provider.getClasses(player).shouldBeEmpty()
    }

    @Test
    fun `when data is not loaded - should return no classes and not set class`() {
        isLoaded = false

        provider.getClasses(player).shouldBeEmpty()
        provider.setClass(player, "warrior", slotId = null).shouldBeFalse()
    }

    @Test
    fun `when set class - should call event and select the class`() {
        provider.setClass(player, "arcane_mage", slotId = "class").shouldBeTrue()

        events.map { it.newClass } shouldContainExactly listOf(arcaneMage)
        events.single().reason shouldBe PlayerChangeClassEvent.Reason.COMMAND_SELECT
        selectedClasses shouldContainExactly listOf(arcaneMage)
    }

    @Test
    fun `when class change is cancelled - should not select the class`() {
        isEventCancelled = true

        provider.setClass(player, "warrior", slotId = null).shouldBeTrue()
        selectedClasses.shouldBeEmpty()
    }

    @Test
    fun `when set the current class - should do nothing`() {
        provider.setClass(player, "paladin", slotId = null).shouldBeTrue()

        events.shouldBeEmpty()
        selectedClasses.shouldBeEmpty()
    }

    @Test
    fun `when set unknown or default class - should not set it`() {
        provider.setClass(player, "unknown", slotId = null).shouldBeFalse()
        provider.setClass(player, "human", slotId = null).shouldBeFalse()
        selectedClasses.shouldBeEmpty()
    }

    private fun mockClass(
        id: String,
        name: String = id,
        subclasses: List<PlayerClass> = emptyList(),
    ): PlayerClass = mockk {
        every { this@mockk.id } returns id
        every { this@mockk.name } returns name
        every { this@mockk.subclasses } returns subclasses.map { Subclass(it, 1) }
    }
}
