// Copyright (c) 2018 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.internal

import java.util.logging.Level
import java.util.logging.Logger

internal object Log {

    private const val DEBUG_TAG = "[DEBUG]"

    private var logger: SimpleLogger? = null
    private var debug = false

    /**
     * Initializes Log with the given logger and specified debug mode.
     */
    fun init(logger: Logger, debug: Boolean = false) {
        init(logger::log, debug)
    }

    /**
     * Initializes Log with the given logger and specified debug mode.
     */
    fun init(logger: SimpleLogger, debug: Boolean = false) {
        this.logger = logger
        this.debug = debug
    }

    /**
     * Write info message to log.
     */
    fun i(message: String) {
        logger?.log(Level.INFO, message)
    }

    /**
     * Writes warning messages to log.
     */
    fun w(message: String) {
        logger?.log(Level.WARNING, message)
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
            logger?.log(Level.INFO, "$DEBUG_TAG $message")
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
            logger?.log(Level.WARNING, "Error occurred. Enable debug mode to see it.")
        }
    }
}

internal fun interface SimpleLogger {
    fun log(level: Level, message: String) = log(level, message, null)
    fun log(level: Level, message: String?, throwable: Throwable?)
}
