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
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicesManager
import org.bukkit.plugin.SimpleServicesManager
import ru.endlesscode.mimic.internal.Log
import java.util.*

/** Base for all Bukkit-related tests. */
@Suppress("UnstableApiUsage")
open class BukkitTestBase {
    protected val plugin: Plugin = mockPlugin(server)
    protected val player: Player = mockPlayer()
    protected val servicesManager: ServicesManager = server.servicesManager

    init {
        Log.init({ level, message, throwable ->
            println("$level: $message")
            throwable?.printStackTrace()
        })

        mockBukkit()
    }

    private fun mockPlugin(mockServer: Server): Plugin = mockk {
        every { server } returns mockServer
        every { pluginMeta } returns mockk {
            every { authors } returns listOf("Plugin Author")
        }
    }

    private fun mockPlayer(): Player = mockk(relaxUnitFun = true) {
        every { uniqueId } returns UUID.randomUUID()
    }

    private companion object {
        val server: Server = mockServer()

        private fun mockServer(): Server = mockk {
            every { name } returns "MockServer"
            every { version } returns "0.0.0"
            every { bukkitVersion } returns "0.0.0"

            every { pluginManager } returns mockk(relaxUnitFun = true)
            every { servicesManager } returns SimpleServicesManager()
            every { isPrimaryThread } returns true
            every { logger } returns mockk(relaxUnitFun = true)
        }

        fun mockBukkit() {
            @Suppress("SENSELESS_COMPARISON") // The annotation lies about nullability
            if (Bukkit.getServer() == null) Bukkit.setServer(server)
        }
    }
}
