// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.papermc.paper.ServerBuildInfo
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.SimpleServicesManager
import ru.endlesscode.mimic.internal.Log
import java.util.*

/** Base for all Bukkit-related tests. */
@Suppress("UnstableApiUsage")
open class BukkitTestBase {
    protected val plugin: Plugin = mockPlugin(server)
    protected val player: Player = mockPlayer()

    init {
        Log.init({ level, message, throwable ->
            println("$level: $message")
            throwable?.printStackTrace()
        })

        mockBukkit()
    }

    private fun mockPlugin(mockServer: Server): Plugin = mockk {
        every { name } returns "Mimic"
        every { isEnabled } returns true
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
            if (Bukkit.getServer() == null) {
                // Bukkit.setServer logs the server version provided by the server implementation
                mockkStatic(ServerBuildInfo::class)
                every { ServerBuildInfo.buildInfo() } returns mockk(relaxed = true)
                Bukkit.setServer(server)
            }
        }
    }
}
