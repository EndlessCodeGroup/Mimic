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

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {

    private static Logger logger;
    private static boolean debug;

    private Log() {
        // Can't be instantiated
    }

    /**
     * Wraps given logger with disabled debug mode.
     *
     * @param logger The logger
     */
    public static void wrap(Logger logger) {
        wrap(logger, false);
    }

    /**
     * Wraps given logger. Also changes debug mode.
     *
     * @param logger The logger
     * @param debug Debug mode
     */
    public static void wrap(Logger logger, boolean debug) {
        Log.logger = logger;
        Log.debug = debug;
    }

    /**
     * Write info message to log.
     *
     * @param message The message
     */
    public static void i(String message) {
        logger.info(message);
    }

    /**
     * Write a message to log if debug is enabled.
     *
     * @param message Debug message
     */
    public static void d(String message, Object... args) {
        if (debug) {
            logger.warning("[DEBUG] " + String.format(message, args));
        }
    }

    /**
     * Write an exception to log if debug is enabled.
     *
     * @param throwable Thrown exception
     */
    public static void d(Throwable throwable) {
        d(throwable, false);
    }

    /**
     * Write an exception to log if debug is enabled.
     *
     * @param throwable Thrown exception
     * @param quiet Print warning to not debug console if quiet - false
     */
    public static void d(Throwable throwable, boolean quiet) {
        if (debug) {
            logger.log(Level.WARNING, "[DEBUG] Yay! Long-awaited exception!", throwable);
        } else if (!quiet) {
            logger.warning("Error occurred. You can see it in debug mode.");
        }
    }
}
