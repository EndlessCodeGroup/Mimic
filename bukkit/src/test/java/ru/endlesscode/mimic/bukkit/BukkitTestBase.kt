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

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import org.bukkit.plugin.*
import ru.endlesscode.mimic.bukkit.util.Log
import kotlin.test.BeforeTest

/** Base for all Bukkit-related tests. */
open class BukkitTestBase {
    protected lateinit var server: Server
    protected lateinit var plugin: Plugin
    protected lateinit var player: Player

    @BeforeTest
    open fun setUp() {
        mockServer()
        mockPlugin()
        mockPlayer()
        mockBukkit()
    }

    private fun mockServer() {
        server = mock { server ->
            on { name } doReturn "TestBukkit"
            on { version } doReturn "1.0"
            on { bukkitVersion } doReturn "1.9.4"
            on { logger } doReturn Log.TEST_LOGGER

            val servicesManager: ServicesManager = SimpleServicesManager()
            on(server.servicesManager) doReturn servicesManager

            val commandMap = SimpleCommandMap(server)
            val pluginManager: PluginManager = SimplePluginManager(server, commandMap)
            on(server.pluginManager) doReturn pluginManager
        }
    }

    private fun mockPlugin() {
        plugin = mock {
            on { server } doReturn server
        }
    }

    private fun mockPlayer() {
        player = mock()
    }

    private fun mockBukkit() {
        val serverField = Bukkit::class.java.getDeclaredField("server")
        serverField.isAccessible = true
        serverField.set(null, server)
    }
}
