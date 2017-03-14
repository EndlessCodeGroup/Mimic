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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Testing validation utils
 *
 * @author Osip Fatkullin
 * @since 1.0
 */
public class ValidateTest {
    @Test
    public void testNotNullPassingNull() throws Exception {
        try {
            Validate.notNull(null);
            fail("State must be illegal!");
        } catch (IllegalStateException e) {
            assertEquals("The validated object is null", e.getMessage());
        }
    }

    @Test
    public void testNotNullPassingNullWithComment() throws Exception {
        try {
            Validate.notNull(null, "Custom message");
            fail("State must be illegal!");
        } catch (IllegalStateException e) {
            assertEquals("Custom message", e.getMessage());
        }
    }

    @Test
    public void testNotNullPassingNotNull() throws Exception {
        Validate.notNull(new Object());
    }
}