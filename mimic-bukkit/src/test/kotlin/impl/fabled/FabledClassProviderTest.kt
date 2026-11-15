// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.fabled

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import ru.endlesscode.mimic.BukkitTestBase
import studio.magemonkey.fabled.api.classes.FabledClass
import studio.magemonkey.fabled.api.player.PlayerClass
import studio.magemonkey.fabled.api.player.PlayerData
import kotlin.test.Test

internal class FabledClassProviderTest : BukkitTestBase() {

    private val warrior = mockClass("Warrior")
    private val honorGuard = mockClass("Honor Guard", prefix = "§6Honor Guard", parent = warrior)
    private val mage = mockClass("Mage")
    private val elf = mockClass("Elf", group = "Race")

    private var isLoaded = true
    private val playerClasses = mutableMapOf("class" to mockPlayerClass(warrior))

    private val data: PlayerData = mockk {
        every { mainClass } answers { playerClasses["class"] }
        every { getClass(any()) } answers { playerClasses[firstArg()] }
        every { canProfess(any()) } returns false
        every { profess(any()) } returns true
    }

    private val fabled: FabledWrapper = mockk(relaxUnitFun = true) {
        every { getLoadedData(player) } answers { if (isLoaded) data else null }
        every { groups } returns listOf("class", "Race")
        every { classes } returns listOf(warrior, honorGuard, mage, elf)
    }

    // SUT
    private val provider = FabledClassProvider(fabled)

    @Test
    fun `when get all slots - should return groups with converted IDs`() {
        provider.allSlots.map { it.id } shouldContainExactly listOf("class", "race")
    }

    @Test
    fun `when get all classes - should convert IDs and link parents and group tracks`() {
        val classes = provider.allClasses

        classes.map { it.id } shouldContainExactly listOf("warrior", "honor_guard", "mage", "elf")
        classes.associate { it.id to it.parents } shouldBe mapOf(
            "warrior" to emptySet(),
            "honor_guard" to setOf("warrior"),
            "mage" to emptySet(),
            "elf" to emptySet(),
        )
        classes.map { it.levelTrack } shouldContainExactly listOf("class", "class", "class", "race")
    }

    @Test
    fun `when get classes - should skip empty groups`() {
        provider.getClasses(player).mapValues { it.value.id } shouldBe mapOf("class" to "warrior")
        provider.getPrimarySlot(player)?.id shouldBe "class"

        playerClasses["Race"] = mockPlayerClass(elf)
        provider.getClasses(player).mapValues { it.value.id } shouldBe mapOf("class" to "warrior", "race" to "elf")
    }

    @Test
    fun `when data is not loaded - should return no classes and not set class`() {
        isLoaded = false

        provider.getClasses(player).shouldBeEmpty()
        provider.getPrimarySlot(player).shouldBeNull()
        provider.setClass(player, "mage", slotId = null).shouldBeFalse()
    }

    @Test
    fun `when player can profess - should profess`() {
        every { data.canProfess(honorGuard) } returns true

        provider.setClass(player, "honor_guard", slotId = "class").shouldBeTrue()

        verify { data.profess(honorGuard) }
        verify(exactly = 0) { fabled.changeClass(any(), any(), any()) }
    }

    @Test
    fun `when player can't profess - should change the class of the group`() {
        provider.setClass(player, "mage", slotId = null).shouldBeTrue()

        verify { fabled.changeClass(player, playerClasses.getValue("class"), mage) }
        verify(exactly = 0) { data.profess(any()) }
    }

    @Test
    fun `when player can't profess into an empty group - should not set class`() {
        playerClasses.clear()

        provider.setClass(player, "honor_guard", slotId = null).shouldBeFalse()
    }

    @Test
    fun `when set the current class - should do nothing`() {
        provider.setClass(player, "warrior", slotId = null).shouldBeTrue()

        verify(exactly = 0) {
            data.profess(any())
            fabled.changeClass(any(), any(), any())
        }
    }

    @Test
    fun `when class is unknown or belongs to another group - should not set it`() {
        provider.setClass(player, "unknown", slotId = null).shouldBeFalse()
        provider.setClass(player, "elf", slotId = "class").shouldBeFalse()
    }

    private fun mockClass(
        name: String,
        prefix: String = name,
        group: String = "class",
        parent: FabledClass? = null,
    ): FabledClass = mockk {
        every { this@mockk.name } returns name
        every { this@mockk.prefix } returns prefix
        every { this@mockk.group } returns group
        every { this@mockk.parent } returns parent
    }

    private fun mockPlayerClass(fabledClass: FabledClass): PlayerClass = mockk {
        every { data } returns fabledClass
    }
}
