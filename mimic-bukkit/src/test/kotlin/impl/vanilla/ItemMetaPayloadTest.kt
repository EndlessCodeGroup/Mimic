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

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.bukkit.inventory.ItemFlag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream
import kotlin.test.Test

internal class ItemMetaPayloadTest {

    @ParameterizedTest
    @ValueSource(strings = ["", "{}", "invalid:::"])
    fun `parse - empty or invalid object - should return null`(input: String) {
        ItemMetaPayload.parse(input).shouldBeNull()
    }

    @Test
    fun `parse - flags in lowercase - should be case parsed`() {
        ItemMetaPayload.parse("flags: [hide_attributes, hide_dye]")
            .shouldBe(ItemMetaPayload(flags = setOf(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE)))
    }

    @ParameterizedTest
    @MethodSource("validData")
    fun `parse - valid input - should return configured object`(input: String, output: ItemMetaPayload) {
        ItemMetaPayload.parse(input) shouldBe output
    }

    companion object {

        @JvmStatic
        fun validData(): Stream<Arguments> = Stream.of(
            // Simple cases
            arguments("{name: Name}", ItemMetaPayload(name = "Name")),
            arguments("name=Name", ItemMetaPayload(name = "Name")),
            arguments(
                """
                    name = Name,
                    lore = [Line1, Line2],
                    unbreakable = true,
                    damage = 42,
                    custom-model-data = 24,
                    flags = [HIDE_ATTRIBUTES, HIDE_DYE]
                """,
                ItemMetaPayload(
                    name = "Name",
                    lore = listOf("Line1", "Line2"),
                    isUnbreakable = true,
                    damage = 42,
                    customModelData = 24,
                    flags = setOf(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE),
                )
            ),
            // Unknown fields
            arguments("unknown: only", ItemMetaPayload()),
            arguments("unknown: and, damage: 40", ItemMetaPayload(damage = 40)),
        )
    }
}
