/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2018 Osip Fatkullin
 * Copyright (C) 2018 EndlessCode Group and contributors
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

package ru.endlesscode.mimic.bukkit

import org.bukkit.plugin.ServicePriority
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.endlesscode.mimic.api.system.registry.SubsystemPriority
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class ToServicePriorityTest(
    private val servicePriority: ServicePriority,
    private val subsystemPriority: SubsystemPriority
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            arrayOf(ServicePriority.Lowest, SubsystemPriority.LOWEST),
            arrayOf(ServicePriority.Low, SubsystemPriority.LOW),
            arrayOf(ServicePriority.Normal, SubsystemPriority.NORMAL),
            arrayOf(ServicePriority.High, SubsystemPriority.HIGH),
            arrayOf(ServicePriority.Highest, SubsystemPriority.HIGHEST)
        )
    }

    @Test
    fun testToServicePriority() {
        assertEquals(servicePriority, subsystemPriority.toServicePriority())
    }
}
