/*
 * This file is part of MimicAPI.
 * Copyright (C) 2018 Osip Fatkullin
 * Copyright (C) 2018 EndlessCode Group and contributors
 *
 * MimicAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MimicAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MimicAPI.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.api.system.registry

import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class SubsystemPriorityTest(
    private val priority: SubsystemPriority,
    private val stringValue: String
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            arrayOf(SubsystemPriority.HIGHEST, "HIGHEST"),
            arrayOf(SubsystemPriority.HIGHEST, "Highest"),
            arrayOf(SubsystemPriority.HIGH, "HIGH"),
            arrayOf(SubsystemPriority.HIGH, "High"),
            arrayOf(SubsystemPriority.NORMAL, "NORMAL"),
            arrayOf(SubsystemPriority.NORMAL, "Normal"),
            arrayOf(SubsystemPriority.LOW, "LOW"),
            arrayOf(SubsystemPriority.LOW, "Low"),
            arrayOf(SubsystemPriority.LOWEST, "LOWEST"),
            arrayOf(SubsystemPriority.LOWEST, "Lowest")
        )
    }

    @Test
    fun testFromString() {
        // When
        val priority = SubsystemPriority.fromString(stringValue)

        // Then
        assertEquals(this.priority, priority)
    }
}
