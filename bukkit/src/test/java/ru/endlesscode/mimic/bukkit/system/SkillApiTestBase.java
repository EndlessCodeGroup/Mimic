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

import com.sucy.skill.*;
import com.sucy.skill.api.classes.*;
import com.sucy.skill.api.player.*;
import com.sucy.skill.data.*;
import org.junit.*;
import org.powermock.core.classloader.annotations.*;
import ru.endlesscode.mimic.bukkit.*;

import java.util.*;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({SkillAPI.class, PlayerData.class, PlayerClass.class})
public class SkillApiTestBase extends BukkitTestBase {
    protected PlayerData data;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        mockStatic(SkillAPI.class);
        mockSettings();
        mockPlayerData();
    }

    private void mockSettings() {
        Settings settings = mock(Settings.class);
        // Our formula exp to next level is: currentLvl * 10
        when(settings.getRequiredExp(anyInt())).then(invocation -> (int) invocation.getArgument(0) * 10);
        when(SkillAPI.getSettings()).thenReturn(settings);
    }

    private void mockPlayerData() {
        data = mock(PlayerData.class);
        when(SkillAPI.getPlayerData(this.player)).thenReturn(data);
    }

    protected void prepareClasses(String... classNames) {
        List<PlayerClass> playerClasses = new ArrayList<>();
        for (String className : classNames) {
            PlayerClass playerClass = mockNamedPlayerClass(className);
            playerClasses.add(playerClass);
        }

        when(data.getClasses()).thenReturn(playerClasses);
        when(data.getMainClass()).thenReturn(playerClasses.isEmpty() ? null : playerClasses.get(0));
    }

    private PlayerClass mockNamedPlayerClass(String className) {
        PlayerClass playerClass = mock(PlayerClass.class);
        RPGClass rpgClass = mock(RPGClass.class);
        when(rpgClass.getName()).thenReturn(className);
        when(playerClass.getData()).thenReturn(rpgClass);

        return playerClass;
    }
}
