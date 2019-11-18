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

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {

    private static final String DEBUG_TAG = "[DEBUG]";

    private static Logger logger;
    private static boolean debug;

    private Log() {
        // Can't be instantiated
    }

    /**
     * Initializes Log with the given logger with disabled debug mode.
     */
    public static void init(Logger logger) {
        init(logger, false);
    }

    /**
     * Initializes Log with the given logger and specified debug mode.
     */
    public static void init(Logger logger, boolean debug) {
        Log.logger = logger;
        Log.debug = debug;
    }

    /**
     * Write info message to log.
     */
    public static void i(String message) {
        logger.info(message);
    }

    /**
     * Writes warning message to log.
     */
    public static void w(String message, Object... args) {
        logger.warning(MessageFormat.format(message, args));
    }

    /**
     * Write a message to log if debug is enabled.
     *
     * @param message Debug message
     */
    public static void d(String message, Object... args) {
        if (debug) {
            logger.fine(DEBUG_TAG + " " + MessageFormat.format(message, args));
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
            logger.log(Level.FINE, DEBUG_TAG + " Yay! Long-awaited exception!", throwable);
        } else if (!quiet) {
            logger.warning("Error occurred. You can see it in debug mode.");
        }
    }
}
