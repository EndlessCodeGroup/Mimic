/*
 * This file is part of MimicAPI.
 * Copyright (C) 2017 Osip Fatkullin
 *
 * MimicAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MimicAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MimicAPI.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.util;

/**
 * This is class for validating object states.
 *
 * <p>In the class used lines from Apache Commons Lang. If an argument state is
 * invalid, an IllegalStateException is thrown.</p>
 *
 * @author Osip Fatkullin
 * @since 1.0
 */
public class Validate {
    /**
     * Validate that the specified argument is not {@code null};
     * otherwise throwing an exception.
     *
     * <pre>
     *     {@code Validate.notNull(myObject);}
     * </pre>
     *
     * <p>The message of the exception is "The validated object is
     * null".</p>
     *
     * @param object    The object to check
     * @throws IllegalStateException if the object is {@code null}
     */
    public static void notNull(Object object) {
        notNull(object, "The validated object is null");
    }

    /**
     * Validate that the specified argument is not {@code null};
     * otherwise throwing an exception with the specified message.
     *
     * <pre>
     *     {@code Validate.notNull(myObject, "The object must not be null");}
     * </pre>
     *
     * @param object    The object to check
     * @param message   The exception message if invalid
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalStateException(message);
        }
    }
}
