/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2020 Osip Fatkullin
 * Copyright (C) 2020 EndlessCode Group and contributors
 *
 * BukkitMimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BukkitMimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BukkitMimic.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.impl.mimic

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.ServicePriority.Lowest
import org.bukkit.plugin.ServicePriority.Normal
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import kotlin.test.Test

internal class MimicItemsRegistryTest : BukkitTestBase() {

    // SUT
    private val registries = ServiceRegistry<BukkitItemsRegistry>("ItemsRegistry", plugin, defaultId = "minecraft")
    private val itemsService: BukkitItemsRegistry = MimicItemsRegistry(registries)

    init {
        registries.register(TestItemRegistry(), plugin, Lowest)
    }

    @ParameterizedTest
    @ValueSource(strings = ["acacia_boat", "test:acacia_boat"])
    fun `when check is same item - should return true`(itemId: String) {
        val item = ItemStack(Material.ACACIA_BOAT)
        itemsService.isSameItem(item, itemId).shouldBeTrue()
    }

    @ParameterizedTest
    @ValueSource(strings = ["acacia_boat", "test:acacia_boat", "mimic:acacia_boat", "test:ACACIA_BOAT", "Test:acacia_boat"])
    fun `when get item - should return item stack`(itemId: String) {
        val item = itemsService.getItem(itemId).shouldNotBeNull()

        assertSoftly {
            item.type shouldBe Material.ACACIA_BOAT
            item.amount shouldBe 1
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["ns:acacia_boat", "test:unknown", "42", "test:acacia boat", ":acacia_boat", "test:"])
    fun `when get unknown item - should return null`(itemId: String) {
        itemsService.getItem(itemId).shouldBeNull()
    }

    @Test
    fun `when get id - should return id`() {
        val item = ItemStack(Material.ACACIA_BOAT)
        itemsService.getItemId(item) shouldBe "test:acacia_boat"
    }

    @ParameterizedTest
    @CsvSource(
        "air,           true",
        "ns:air,        false",
        "test:air,      true",
        "unknown,       false",
        "gold_sword,    false",
        "golden_sword,  true",
        "mimic:air,     true",
        "test:AIR,      true",
        "test:air!,     false",
    )
    fun `when check is item exists`(itemId: String, shouldExist: Boolean) {
        itemsService.isItemExists(itemId) shouldBe shouldExist
    }

    @ParameterizedTest
    @ValueSource(strings = ["test:acacia boat", ":acacia_boat", "test:", "test:acacia_boat!"])
    fun `when use invalid id - should not query registries`(itemId: String) {
        val registry = mockk<BukkitItemsRegistry>(relaxed = true) {
            every { id } returns "mock"
            every { isEnabled } returns true
        }
        registries.register(registry, plugin, Normal)

        assertSoftly {
            itemsService.isItemExists(itemId) shouldBe false
            itemsService.getItem(itemId).shouldBeNull()
            itemsService.isSameItem(ItemStack(Material.ACACIA_BOAT), itemId) shouldBe false
        }
        verify(exactly = 0) {
            registry.isItemExists(any())
            registry.getItem(any(), any(), any())
            registry.isSameItem(any(), any())
        }
    }

    @Test
    fun `when registry uses legacy ids - should pass them through`() {
        registries.register(LegacyItemRegistry(), plugin, Normal)

        assertSoftly {
            itemsService.knownIds shouldContain "ia:itemsadder:ruby_sword"
            itemsService.getItemId(ItemStack(Material.EMERALD)) shouldBe "ia:itemsadder:ruby_sword"
            itemsService.isItemExists("ia:itemsadder:ruby_sword").shouldBeTrue()
            itemsService.getItem("ia:itemsadder:ruby_sword")?.type shouldBe Material.EMERALD
        }
    }

    @Test
    fun `when registry returns invalid id - should ignore it`() {
        registries.register(InvalidIdItemRegistry(), plugin, Normal)

        assertSoftly {
            itemsService.knownIds shouldNotContain "invalid:Ruby Sword"
            itemsService.knownIds shouldContain "invalid:emerald"
            // Should not fall back to the registry with lower priority
            itemsService.getItemId(ItemStack(Material.EMERALD)).shouldBeNull()
        }
    }

    @Test
    fun `when ids are deprecated or invalid - should warn once`() {
        val warnings = mutableListOf<String>()
        Log.init({ _, message, _ -> warnings += message.orEmpty() })
        registries.register(LegacyItemRegistry(), plugin, Normal)
        registries.register(InvalidIdItemRegistry(), plugin, Normal)

        repeat(2) {
            itemsService.knownIds
            itemsService.getItem("ia:itemsadder:ruby_sword")
            itemsService.getItem("ia:itemsadder:ruby_axe")
            itemsService.getItem("test:ACACIA_BOAT")
        }

        // The first quoted value is the registry ID for registry warnings, and the item ID for input warnings
        warnings.map { it.substringAfter("'").substringBefore("'") } shouldContainExactlyInAnyOrder listOf(
            "ia",
            "invalid",
            "ia:itemsadder:ruby_sword",
            "test:ACACIA_BOAT",
        )
    }
}

private class TestItemRegistry : BukkitItemsRegistry {
    override val id = "test"
    override val knownIds = Material.entries.map { it.name.lowercase() }

    override fun isItemExists(itemId: String): Boolean = itemId in knownIds

    override fun getItemId(item: ItemStack): String = item.type.name.lowercase()

    override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack {
        val material = Material.valueOf(itemId.uppercase())
        return ItemStack(material, amount)
    }
}

/** Registry imitating ItemsAdder returning its own namespace in IDs. */
private class LegacyItemRegistry : BukkitItemsRegistry {
    override val id = "ia"
    override val knownIds = listOf("itemsadder:ruby_sword")

    override fun isItemExists(itemId: String): Boolean = itemId in knownIds

    override fun getItemId(item: ItemStack): String? = knownIds.first().takeIf { item.type == Material.EMERALD }

    override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? =
        if (isItemExists(itemId)) ItemStack(Material.EMERALD, amount) else null
}

/** Registry returning invalid IDs. */
private class InvalidIdItemRegistry : BukkitItemsRegistry {
    override val id = "invalid"
    override val knownIds = listOf("Ruby Sword", "emerald")

    override fun isItemExists(itemId: String): Boolean = itemId in knownIds

    override fun getItemId(item: ItemStack): String? = knownIds.first().takeIf { item.type == Material.EMERALD }

    override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? = null
}
