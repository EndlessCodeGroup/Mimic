// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mmoitems

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ru.endlesscode.mimic.BukkitTestBase
import kotlin.test.Test

internal class MmoItemsItemProviderTest : BukkitTestBase() {

    private val typeNames = mutableMapOf<MMOItemTemplate, String>()
    private val swordExcalibur = mockTemplate("SWORD", "EXCALIBUR")
    private val daggerExcalibur = mockTemplate("DAGGER", "EXCALIBUR")

    private val mmoItems: MmoItemsWrapper = mockk {
        every { templates } returns listOf(swordExcalibur, daggerExcalibur)
        every { getTypeName(any<MMOItemTemplate>()) } answers { typeNames.getValue(firstArg()) }
        every { getTypeName(any<ItemStack>()) } returns null
        every { getId(any()) } returns null
        every { buildItem(swordExcalibur, any()) } answers { ItemStack(Material.DIAMOND_SWORD) }
        every { buildItem(daggerExcalibur, any()) } answers { ItemStack(Material.IRON_SWORD) }
    }

    // SUT
    private val provider = MmoItemsItemProvider(mmoItems)

    @Test
    fun `when get all items - should return ids with item types`() {
        provider.allItems shouldContainExactly listOf("sword/excalibur", "dagger/excalibur")
    }

    @ParameterizedTest
    @CsvSource(
        "sword/excalibur,   DIAMOND_SWORD",
        "dagger/excalibur,  IRON_SWORD",
    )
    fun `when create item - should use the template of the given type`(itemId: String, expected: Material) {
        val item = provider.createItem(itemId, amount = 2, player, payload = null)

        item?.type shouldBe expected
        item?.amount shouldBe 2
        verify { mmoItems.buildItem(any(), player) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["excalibur", "sword/unknown", "axe/excalibur", "sword/excalibur/extra"])
    fun `when create item without type or unknown item - should return null`(itemId: String) {
        provider.createItem(itemId, amount = 1, player = null, payload = null).shouldBeNull()
    }

    @Test
    fun `when get item id - should return id with item type`() {
        every { mmoItems.getTypeName(any<ItemStack>()) } returns "DAGGER"
        every { mmoItems.getId(any()) } returns "EXCALIBUR"

        provider.getItemId(ItemStack(Material.IRON_SWORD)) shouldBe "dagger/excalibur"
    }

    @Test
    fun `when get id of not MMOItems item - should return null`() {
        provider.getItemId(ItemStack(Material.IRON_SWORD)).shouldBeNull()
    }

    private fun mockTemplate(type: String, id: String): MMOItemTemplate =
        mockk<MMOItemTemplate> { every { this@mockk.id } returns id }.also { typeNames[it] = type }
}
