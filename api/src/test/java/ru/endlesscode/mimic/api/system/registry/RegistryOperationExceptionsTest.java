/*
 * This file is part of MimicAPI.
 * Copyright (C) 2018 Osip Fatkullin
 * Copyright (C) 2018 EndlessCode Group and contributors
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

package ru.endlesscode.mimic.api.system.registry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(Parameterized.class)
public class RegistryOperationExceptionsTest {
    private final ExceptionType type;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { ExceptionType.NOT_REGISTERED }, { ExceptionType.NOT_FOUND }, { ExceptionType.NOT_NEEDED }
        });
    }

    public RegistryOperationExceptionsTest(ExceptionType type) {
        this.type = type;
    }

    @Test
    public void testConstructorWithoutParameters() {
        try {
            switch (this.type) {
                case NOT_REGISTERED:
                    throw new SystemNotRegisteredException();
                case NOT_FOUND:
                    throw new SystemNotFoundException();
                default:
                    throw new SystemNotNeededException();
            }
        } catch (RegistryOperationException e) {
            assertNull("Message must be null", e.getMessage());
        }
    }

    @Test
    public void testConstructorWithDetailMessage() {
        try {
            switch (this.type) {
                case NOT_REGISTERED:
                    throw new SystemNotRegisteredException("Message one");
                case NOT_FOUND:
                    throw new SystemNotFoundException("Message one");
                default:
                    throw new SystemNotNeededException("Message one");
            }
        } catch (RegistryOperationException e) {
            assertEquals("Must use message from parameter", "Message one", e.getMessage());
        }
    }

    @Test
    public void testConstructorWithCause() {
        try {
            switch (this.type) {
                case NOT_REGISTERED:
                    throw new SystemNotRegisteredException(new Exception("Message two"));
                case NOT_FOUND:
                    throw new SystemNotFoundException(new Exception("Message two"));
                default:
                    throw new SystemNotNeededException(new Exception("Message two"));
            }
        } catch (RegistryOperationException e) {
            assertEquals("Must use message from cause", "java.lang.Exception: Message two", e.getMessage());
        }
    }

    @Test
    public void testConstructorWithMessageAndCause() {
        try {
            switch (this.type) {
                case NOT_REGISTERED:
                    throw new SystemNotRegisteredException("Message three", new Exception("Message four"));
                case NOT_FOUND:
                    throw new SystemNotFoundException("Message three", new Exception("Message four"));
                default:
                    throw new SystemNotNeededException("Message three", new Exception("Message four"));
            }
        } catch (RegistryOperationException e) {
            assertEquals("Must use message from parameter", "Message three", e.getMessage());
        }
    }

    private enum ExceptionType {
        NOT_REGISTERED, NOT_NEEDED, NOT_FOUND
    }
}
