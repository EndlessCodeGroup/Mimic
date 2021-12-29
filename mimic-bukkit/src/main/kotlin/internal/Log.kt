/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
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

package ru.endlesscode.mimic.internal

import java.util.logging.Level
import java.util.logging.Logger

internal object Log {

    private const val DEBUG_TAG = "[DEBUG]"

    private var logger: Logger? = null
    private var debug = false

    /**
     * Initializes Log with the given logger and specified debug mode.
     */
    fun init(logger: Logger, debug: Boolean = false) {
        this.logger = logger
        this.debug = debug
    }

    /**
     * Write info message to log.
     */
    fun i(message: String) {
        logger?.info(message)
    }

    /**
     * Writes warning message to log.
     */
    fun w(message: String) {
        logger?.warning(message)
    }

    /**
     * Writes warning exception to log.
     */
    fun w(throwable: Throwable, message: String? = throwable.message) {
        logger?.log(Level.WARNING, message, throwable)
    }

    /**
     * Write a message to log if debug is enabled.
     *
     * @param message Debug message
     */
    fun d(message: String) {
        if (debug) {
            logger?.info("$DEBUG_TAG $message")
        }
    }

    /**
     * Write an exception to log if debug is enabled.
     *
     * @param throwable Thrown exception
     */
    fun d(throwable: Throwable) {
        d(throwable, false)
    }

    /**
     * Write an exception to log if debug is enabled.
     *
     * @param throwable Thrown exception
     * @param quiet Print warning to not debug console if quiet - false
     */
    fun d(throwable: Throwable, quiet: Boolean) {
        if (debug) {
            logger?.log(Level.FINE, "$DEBUG_TAG Yay! Long-awaited exception!", throwable)
        } else if (!quiet) {
            logger?.warning("Error occurred. Enable debug mode to see it.")
        }
    }
}
