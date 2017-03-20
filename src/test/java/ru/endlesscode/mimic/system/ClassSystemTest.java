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

package ru.endlesscode.mimic.system;

import org.junit.Before;
import org.junit.Test;
import ru.endlesscode.mimic.system.impl.BasicClassSystem;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Osip Fatkullin
 * @since 1.0
 */
public class ClassSystemTest {
    private BasicClassSystem cs;

    @Before
    public void setUp() {
        this.cs = new BasicClassSystem();
    }

    @Test
    public void testHasClassMustReturnTrue() throws Exception {
        this.cs.setClasses("SomeClass");
        assertTrue(cs.hasClass());


        this.cs.setClasses("SomeClass", "AnotherClass");
        assertTrue(cs.hasClass());
    }

    @Test
    public void testHasClassMustReturnFalse() throws Exception {
        this.cs.setClasses();
        assertFalse(cs.hasClass());

        this.cs.setClasses("");
        assertFalse(cs.hasClass());
    }

    @Test
    public void testHasRequiredClassMustReturnTrue() throws Exception {
        this.cs.setClasses("First", "Second", "Third");

        assertTrue(cs.hasRequiredClass("First"));
        assertTrue(cs.hasRequiredClass("Second"));
        assertTrue(cs.hasRequiredClass("Third"));
    }

    @Test
    public void testHasRequiredClassMustReturnFalse() throws Exception {
        this.cs.setClasses("First", "Second", "Third");

        assertFalse(cs.hasRequiredClass("SomeClass"));
        assertFalse(cs.hasRequiredClass("FirstClass"));
        assertFalse(cs.hasRequiredClass("Sec"));
    }

    @Test
    public void testHasOneOfRequiredClassesMustReturnTrue() throws Exception {
        List<String> requiredClasses = Arrays.asList("First", "Second", "Third");

        this.cs.setClasses("First");
        assertTrue(cs.hasOneOfRequiredClasses(requiredClasses));

        this.cs.setClasses("Second");
        assertTrue(cs.hasOneOfRequiredClasses(requiredClasses));

        this.cs.setClasses("Third");
        assertTrue(cs.hasOneOfRequiredClasses(requiredClasses));
    }

    @Test
    public void testHasOneOfRequiredClassesMustReturnFalse() throws Exception {
        List<String> requiredClasses = Arrays.asList("First", "Second", "Third");

        this.cs.setClasses();
        assertFalse(cs.hasOneOfRequiredClasses(requiredClasses));

        this.cs.setClasses("Fourth");
        assertFalse(cs.hasOneOfRequiredClasses(requiredClasses));
    }

    @Test
    public void testHasAllRequiredClassesMustReturnTrue() throws Exception {
        List<String> requiredClasses = Arrays.asList("First", "Second");

        this.cs.setClasses("First", "Second", "Third");
        assertTrue(cs.hasAllRequiredClasses(requiredClasses));
    }

    @Test
    public void testHasAllRequiredClassesMustReturnFalse() throws Exception {
        List<String> requiredClasses = Arrays.asList("First", "Second");


        this.cs.setClasses();
        assertFalse(cs.hasAllRequiredClasses(requiredClasses));

        this.cs.setClasses("First", "Third");
        assertFalse(cs.hasAllRequiredClasses(requiredClasses));
    }

    @Test
    public void testGetPrimaryClassReturnEmpty() throws Exception {
        this.cs.setClasses();
        assertEquals("", cs.getPrimaryClass());

        this.cs.setClasses("");
        assertEquals("", cs.getPrimaryClass());
    }

    @Test
    public void testGetPrimaryClassReturnProperlyClass() throws Exception {
        this.cs.setClasses("PrimaryClass", "SecondaryClass");
        assertEquals("PrimaryClass", cs.getPrimaryClass());
    }

    @Test
    public void testGetClassesReturnList() throws Exception {
        this.cs.setClasses("First", "Second");
        assertEquals(Arrays.asList("First", "Second"), this.cs.getClasses());
    }

    @Test
    public void testGetClassesReturnEmpty() throws Exception {
        this.cs.setClasses();
        assertTrue(this.cs.getClasses().isEmpty());
    }
}