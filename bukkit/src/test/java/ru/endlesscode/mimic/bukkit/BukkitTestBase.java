/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2017 EndlessCode Group and contributors
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

package ru.endlesscode.mimic.bukkit;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.*;
import org.junit.*;
import org.powermock.core.*;
import org.powermock.reflect.*;
import ru.endlesscode.mimic.bukkit.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base for all Bukkit-related tests.
 */
public class BukkitTestBase {
    protected Server server;
    protected Plugin plugin;
    protected Player player;

    @Before
    public void setUp() throws Exception {
        MockGateway.MOCK_STANDARD_METHODS = false;

        mockServer();
        mockPlugin();
        mockPlayer();
        mockBukkit();
    }

    private void mockServer() {
        this.server = mock(Server.class);
        when(this.server.getName()).thenReturn("TestBukkit");
        when(this.server.getVersion()).thenReturn("1.0");
        when(this.server.getBukkitVersion()).thenReturn("1.9.4");
        when(this.server.getLogger()).thenReturn(Log.TEST_LOGGER);

        ServicesManager servicesManager = new SimpleServicesManager();
        when(this.server.getServicesManager()).thenReturn(servicesManager);

        SimpleCommandMap commandMap = new SimpleCommandMap(this.server);
        PluginManager pluginManager = new SimplePluginManager(this.server, commandMap);
        when(this.server.getPluginManager()).thenReturn(pluginManager);
    }

    private void mockPlugin() {
        this.plugin = mock(Plugin.class);
        when(this.plugin.getServer()).thenReturn(this.server);
    }

    private void mockPlayer() {
        this.player = mock(Player.class);
    }

    private void mockBukkit() {
        Whitebox.setInternalState(Bukkit.class, "server", this.server);
    }
}
