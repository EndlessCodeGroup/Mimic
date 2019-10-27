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

package ru.endlesscode.mimic.bukkit.util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Utility for logging any messages while testing.
 */
public class Log {
    public static final Logger TEST_LOGGER;

    static {
        TEST_LOGGER = Logger.getLogger("BukkitMimic");
        TEST_LOGGER.setUseParentHandlers(false);

        removeAllHandlers();
        addHandlerWithFormatter();
    }

    private static void removeAllHandlers() {
        Handler[] handlers = TEST_LOGGER.getHandlers();

        for (Handler h : handlers) {
            TEST_LOGGER.removeHandler(h);
        }
    }

    private static void addHandlerWithFormatter() {
        Handler handler = new ConsoleHandler();
        handler.setFormatter(new MimicTestLogFormatter());

        TEST_LOGGER.addHandler(handler);
    }
}
