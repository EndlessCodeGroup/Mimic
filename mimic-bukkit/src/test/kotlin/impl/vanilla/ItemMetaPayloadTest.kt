/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
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

package ru.endlesscode.mimic.impl.vanilla

import org.bukkit.inventory.ItemFlag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ItemMetaPayloadTest {

    @ParameterizedTest
    @ValueSource(strings = ["", "{}", "invalid:::", "display-name: 1"])
    fun `parse - empty or invalid object - should return null`(input: String) {
        // When
        val result = ItemMetaPayload.parse(input)

        // Then
        assertNull(result)
    }

    @Test
    fun `parse - flags in lowercase - should be case parsed`() {
        // When
        val result = ItemMetaPayload.parse("item-flags: [hide_attributes, hide_dye]")

        // Then
        assertEquals(ItemMetaPayload(itemFlags = setOf(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE)), result)
    }

    @ParameterizedTest
    @MethodSource("validData")
    fun `parse - valid input - should return configured object`(input: String, output: ItemMetaPayload) {
        // When
        val result = ItemMetaPayload.parse(input)

        // Then
        assertEquals(output, result)
    }

    companion object {

        @JvmStatic
        fun validData(): Stream<Arguments> = Stream.of(
            // Simple cases
            arguments("{display-name: Name}", ItemMetaPayload(displayName = "Name")),
            arguments("display-name=Name", ItemMetaPayload(displayName = "Name")),
            arguments(
                """
                    display-name = Display,
                    localized-name = Localized, 
                    lore = [Line1, Line2],
                    is-unbreakable = true,
                    damage = 42,
                    custom-model-data = 24,
                    item-flags = [HIDE_ATTRIBUTES, HIDE_DYE]
                """.trimIndent(),
                ItemMetaPayload(
                    displayName = "Display",
                    localizedName = "Localized",
                    lore = listOf("Line1", "Line2"),
                    isUnbreakable = true,
                    damage = 42,
                    customModelData = 24,
                    itemFlags = setOf(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE)
                )
            ),
            // Unknown fields
            arguments("unknown: only", ItemMetaPayload()),
            arguments("unknown: and, damage: 40", ItemMetaPayload(damage = 40)),
        )
    }
}
