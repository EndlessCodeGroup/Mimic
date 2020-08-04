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

package impl.mimic

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.ServicePriority
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.impl.mimic.MimicItemsRegistry
import ru.endlesscode.mimic.impl.vanilla.MinecraftItemsRegistry
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import kotlin.test.*

internal class MimicItemsRegistryTest : BukkitTestBase() {

    // SUT
    private lateinit var itemsService: BukkitItemsRegistry

    @BeforeTest
    override fun setUp() {
        super.setUp()
        itemsService = MimicItemsRegistry(servicesManager)

        servicesManager.register(BukkitItemsRegistry::class.java, MinecraftItemsRegistry(), plugin, ServicePriority.Lowest)
        servicesManager.register(BukkitItemsRegistry::class.java, itemsService, plugin, ServicePriority.Highest)
    }

    @ParameterizedTest
    @ValueSource(strings = ["acacia_boat", "minecraft:acacia_boat"])
    fun `when check is same item - should return true`(itemId: String) {
        // Given
        val item = ItemStack(Material.ACACIA_BOAT)

        // When
        val same = itemsService.isSameItem(item, itemId)

        // Then
        assertTrue(same)
    }

    @ParameterizedTest
    @ValueSource(strings = ["acacia_boat", "minecraft:acacia_boat"])
    fun `when get item - should return item stack`(itemId: String) {
        // When
        val item = itemsService.getItem(itemId)!!

        // Then
        assertEquals(Material.ACACIA_BOAT, item.type)
        assertEquals(1, item.amount)
    }

    @ParameterizedTest
    @ValueSource(strings = ["ns:acacia_boat", "minecraft:unknown", "42"])
    fun `when get unknown item - should return null`(itemId: String) {
        // When
        val item = itemsService.getItem(itemId)

        // Then
        assertNull(item)
    }

    @Test
    fun `when get id - should return id`() {
        // Given
        val item = ItemStack(Material.ACACIA_BOAT)

        // When
        val itemId = itemsService.getItemId(item)

        // Then
        assertEquals("minecraft:acacia_boat", itemId)
    }

    @ParameterizedTest
    @CsvSource(
        "air,           true",
        "ns:air,        false",
        "minecraft:air, true",
        "unknown,       false",
        "gold_sword,    false",
        "golden_sword,  true"
    )
    fun `when check is item exists`(itemId: String, shouldExist: Boolean) {
        // When
        val exists = itemsService.isItemExists(itemId)

        // Then
        assertEquals(shouldExist, exists)
    }
}
