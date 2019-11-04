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

package ru.endlesscode.mimic.api.system;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.endlesscode.mockito.MockitoExtra.MOCKS_ONLY_ABSTRACTS;

public class ClassSystemTest {
    private ClassSystem cs;

    @Before
    public void setUp() {
        this.cs = mock(ClassSystem.class, MOCKS_ONLY_ABSTRACTS);
    }

    @Test
    public void testHasClassMustReturnTrue() {
        prepareClasses("SomeClass");
        assertTrue(cs.hasClass());

        prepareClasses("SomeClass", "AnotherClass");
        assertTrue(cs.hasClass());
    }

    @Test
    public void testHasClassMustReturnFalse() {
        prepareClasses();
        assertFalse(cs.hasClass());
    }

    @Test
    public void testHasRequiredClassMustReturnTrue() {
        prepareClasses("First", "Second", "Third");

        assertTrue(cs.hasRequiredClass("First"));
        assertTrue(cs.hasRequiredClass("Second"));
        assertTrue(cs.hasRequiredClass("Third"));
    }

    @Test
    public void testHasRequiredClassMustReturnFalse() {
        prepareClasses("First", "Second", "Third");

        assertFalse(cs.hasRequiredClass("SomeClass"));
        assertFalse(cs.hasRequiredClass("FirstClass"));
        assertFalse(cs.hasRequiredClass("Sec"));
    }

    @Test
    public void testHasOneOfRequiredClassesMustReturnTrue() {
        List<String> requiredClasses = Arrays.asList("First", "Second", "Third");

        prepareClasses("First");
        assertTrue(cs.hasOneOfRequiredClasses(requiredClasses));

        prepareClasses("Second");
        assertTrue(cs.hasOneOfRequiredClasses(requiredClasses));

        prepareClasses("Third");
        assertTrue(cs.hasOneOfRequiredClasses(requiredClasses));
    }

    @Test
    public void testHasOneOfRequiredClassesMustReturnFalse() {
        List<String> requiredClasses = Arrays.asList("First", "Second", "Third");

        prepareClasses();
        assertFalse(cs.hasOneOfRequiredClasses(requiredClasses));

        prepareClasses("Fourth");
        assertFalse(cs.hasOneOfRequiredClasses(requiredClasses));
    }

    @Test
    public void testHasAllRequiredClassesMustReturnTrue() {
        List<String> requiredClasses = Arrays.asList("First", "Second");

        prepareClasses("First", "Second", "Third");
        assertTrue(cs.hasAllRequiredClasses(requiredClasses));
    }

    @Test
    public void testHasAllRequiredClassesMustReturnFalse() {
        List<String> requiredClasses = Arrays.asList("First", "Second");


        prepareClasses();
        assertFalse(cs.hasAllRequiredClasses(requiredClasses));

        prepareClasses("First", "Third");
        assertFalse(cs.hasAllRequiredClasses(requiredClasses));
    }

    @Test
    public void testGetPrimaryClassReturnEmpty() {
        prepareClasses();
        assertNull(cs.getPrimaryClass());

        prepareClasses("");
        assertEquals("", cs.getPrimaryClass());
    }

    @Test
    public void testGetPrimaryClassReturnProperlyClass() {
        prepareClasses("PrimaryClass", "SecondaryClass");
        assertEquals("PrimaryClass", cs.getPrimaryClass());
    }

    private void prepareClasses(String... classes) {
        when(cs.getClasses()).thenReturn(Arrays.asList(classes));
    }
}
