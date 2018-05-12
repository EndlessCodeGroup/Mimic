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

import java.io.*;
import java.text.*;
import java.util.logging.*;

/**
 * Formatter for test logger.
 */
class MimicTestLogFormatter extends Formatter {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        StringBuilder messageBuilder = new StringBuilder();

        String dateString = DATE_FORMAT.format(record.getMillis());
        messageBuilder.append("[").append(dateString).append("] [")
                .append(record.getLoggerName()).append("] [")
                .append(record.getLevel().getLocalizedName()).append("] ")
                .append(record.getMessage()).append('\n');

        Throwable exception = record.getThrown();
        if (exception != null) {
            appendExceptionStackTrace(messageBuilder, exception);
        }

        return messageBuilder.toString();
    }

    private void appendExceptionStackTrace(StringBuilder messageBuilder, Throwable exception) {
        StringWriter exceptionWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(exceptionWriter));
        messageBuilder.append(exceptionWriter);
    }
}
