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
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.ServicePriority.Highest
import org.bukkit.plugin.ServicePriority.Lowest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.impl.vanilla.MinecraftItemsRegistry
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import kotlin.test.Test

internal class MimicItemsRegistryTest : BukkitTestBase() {

    // SUT
    private val itemsService: BukkitItemsRegistry = MimicItemsRegistry(servicesManager)

    init {
        servicesManager.register(BukkitItemsRegistry::class.java, MinecraftItemsRegistry(), plugin, Lowest)
        servicesManager.register(BukkitItemsRegistry::class.java, itemsService, plugin, Highest)
    }

    @ParameterizedTest
    @ValueSource(strings = ["acacia_boat", "minecraft:acacia_boat"])
    fun `when check is same item - should return true`(itemId: String) {
        val item = ItemStack(Material.ACACIA_BOAT)
        itemsService.isSameItem(item, itemId).shouldBeTrue()
    }

    @ParameterizedTest
    @ValueSource(strings = ["acacia_boat", "minecraft:acacia_boat"])
    fun `when get item - should return item stack`(itemId: String) {
        val item = itemsService.getItem(itemId).shouldNotBeNull()

        assertSoftly {
            item.type shouldBe Material.ACACIA_BOAT
            item.amount shouldBe 1
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["ns:acacia_boat", "minecraft:unknown", "42"])
    fun `when get unknown item - should return null`(itemId: String) {
        itemsService.getItem(itemId).shouldBeNull()
    }

    @Test
    fun `when get id - should return id`() {
        val item = ItemStack(Material.ACACIA_BOAT)
        itemsService.getItemId(item) shouldBe "minecraft:acacia_boat"
    }

    @ParameterizedTest
    @CsvSource(
        "air,           true",
        "ns:air,        false",
        "minecraft:air, true",
        "unknown,       false",
        "gold_sword,    false",
        "golden_sword,  true",
    )
    fun `when check is item exists`(itemId: String, shouldExist: Boolean) {
        itemsService.isItemExists(itemId) shouldBe shouldExist
    }
}
