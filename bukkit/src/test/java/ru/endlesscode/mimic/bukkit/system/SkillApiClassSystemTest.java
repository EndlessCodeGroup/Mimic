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

package ru.endlesscode.mimic.bukkit.system;

import com.sucy.skill.SkillAPI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.endlesscode.mimic.api.system.ClassSystem;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class SkillApiClassSystemTest extends SkillApiTestBase {
    private ClassSystem classSystem;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        this.classSystem = SkillApiClassSystem.FACTORY.get(this.player);
    }

    @Test
    public void testGetClassesMustReturnRightClasses() {
        String[] expectedClasses = new String[]{"Mage", "Cleric"};
        prepareClasses(expectedClasses);

        List<String> actualClasses = classSystem.getClasses();
        assertEquals(Arrays.asList(expectedClasses), actualClasses);
    }

    @Test
    public void testGetClassesMustReturnEmptyList() {
        prepareClasses();

        List<String> actualClasses = classSystem.getClasses();
        assertTrue(actualClasses.isEmpty());
    }

    @Test
    public void testGetPrimaryClassReturnsRightClass() {
        prepareClasses("Primary", "Secondary", "Third");

        String actualPrimaryClass = classSystem.getPrimaryClass();
        assertEquals("Primary", actualPrimaryClass);
    }

    @Test
    public void testGetPrimaryClassReturnsEmptyString() {
        prepareClasses();

        String actualPrimaryClass = classSystem.getPrimaryClass();
        assertEquals("", actualPrimaryClass);
    }

    @Test
    public void testIsEnabledReturnsStatusOfSkillApi() {
        when(SkillAPI.isLoaded()).thenReturn(true).thenReturn(false);

        assertTrue(this.classSystem.isEnabled());
        assertFalse(this.classSystem.isEnabled());
    }

    @Test
    public void testGetNameAlwaysReturnSkillAPI() {
        assertEquals("SkillAPI", this.classSystem.getName());
    }
}
