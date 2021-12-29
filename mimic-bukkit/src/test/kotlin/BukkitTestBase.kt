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

package ru.endlesscode.mimic

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicesManager
import org.bukkit.plugin.SimpleServicesManager
import java.util.*

/** Base for all Bukkit-related tests. */
open class BukkitTestBase {
    protected val server: Server = mockServer()
    protected val plugin: Plugin = mockPlugin(server)
    protected val player: Player = mockPlayer()
    protected val servicesManager: ServicesManager = server.servicesManager

    init {
        mockBukkit()
    }

    private fun mockServer(): Server = mockk {
        every { pluginManager } returns mockk(relaxUnitFun = true)
        every { servicesManager } returns SimpleServicesManager()
    }

    private fun mockPlugin(mockServer: Server): Plugin = mockk {
        every { server } returns mockServer
    }

    private fun mockPlayer(): Player = mockk(relaxUnitFun = true) {
        every { uniqueId } returns UUID.randomUUID()
    }

    private fun mockBukkit() {
        mockkStatic(Bukkit::class)
        every { Bukkit.getServer() } returns server
    }
}
