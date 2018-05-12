/*
 * This file is part of MimicAPI.
 * Copyright (C) 2017 Osip Fatkullin
 * Copyright (C) 2017 EndlessCode Group and Contributors
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

package ru.endlesscode.mimic.api.ref;

import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * Testing ExistingWeakReference class.
 *
 * @author Osip Fatkullin
 * @since 1.0
 */
public class ExistingWeakReferenceTest {
    private Object obj;

    @Before
    public void setUp() {
        this.obj = new Object();
    }

    @Test
    public void testGettingNotNullReferent() {
        ExistingWeakReference<Object> ref = new ExistingWeakReference<>(obj);
        Object givenObj = ref.get();

        assertEquals(obj, givenObj);
    }

    @Test(expected = IllegalStateException.class)
    public void testGettingNullReferenceMustThrowException() {
        ExistingWeakReference<Object> ref = new ExistingWeakReference<>(obj);
        ref.clear();

        ref.get();
    }
}
