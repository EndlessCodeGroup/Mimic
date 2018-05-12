/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2017 EndlessCode Group and contributors
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

package ru.endlesscode.mimic.bukkit.system;

import org.bukkit.permissions.*;
import org.junit.*;
import ru.endlesscode.mimic.api.system.*;
import ru.endlesscode.mimic.bukkit.*;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PermissionsClassSystemTest extends BukkitTestBase {
    private ClassSystem classSystem;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        this.classSystem = PermissionsClassSystem.FACTORY.get(this.player);
    }

    @Test
    public void testHasRequiredClassMustReturnTrue() {
        this.addClasses("first", "second", "third");

        assertTrue(this.classSystem.hasRequiredClass("first"));
        assertTrue(this.classSystem.hasRequiredClass("First"));
        assertTrue(this.classSystem.hasRequiredClass("second"));
        assertTrue(this.classSystem.hasRequiredClass("Second"));
        assertTrue(this.classSystem.hasRequiredClass("third"));
        assertTrue(this.classSystem.hasRequiredClass("Third"));
    }

    @Test
    public void testHasRequiredClassMustReturnFalse() {
        this.addClasses("first", "second");

        assertFalse(this.classSystem.hasRequiredClass("third"));
        assertFalse(this.classSystem.hasRequiredClass("FirstClass"));
        assertFalse(this.classSystem.hasRequiredClass("Sec"));
        assertFalse(this.classSystem.hasRequiredClass(""));
    }

    private void addClasses(String... classes) {
        for (String theClass : classes) {
            when(this.player.hasPermission(PermissionsClassSystem.PERMISSION_PREFIX + theClass)).thenReturn(true);
        }
    }

    @Test
    public void testGetClassesReturnList() {
        List<PermissionAttachmentInfo> perms = Arrays.asList(
                createClassPermissionMock("first", true),
                createClassPermissionMock("second", false),
                createClassPermissionMock("third", true),
                createPermissionMock("mimic.other.permission", true));
        when(this.player.getEffectivePermissions()).thenReturn(new HashSet<>(perms));

        List<String> expected = Arrays.asList("first", "third");
        List<String> actual = this.classSystem.getClasses();
        assertTrue(expected.containsAll(actual) && actual.containsAll(expected));
    }

    private PermissionAttachmentInfo createClassPermissionMock(String theClass, boolean value) {
        String classPermission = PermissionsClassSystem.PERMISSION_PREFIX + theClass;
        return createPermissionMock(classPermission, value);
    }

    private PermissionAttachmentInfo createPermissionMock(String permission, boolean value) {
        PermissionAttachmentInfo perm = mock(PermissionAttachmentInfo.class);
        when(perm.getPermission()).thenReturn(permission);
        when(perm.getValue()).thenReturn(value);

        return perm;
    }

    @Test
    public void testIsEnabledAlwaysReturnTrue() {
        assertTrue(this.classSystem.isEnabled());
    }

    @Test
    public void testNameAlwaysReturnPermissionClassSystem() {
        assertEquals("Permission Class System", this.classSystem.getName());
    }
}
