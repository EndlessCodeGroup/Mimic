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

import org.bukkit.plugin.ServicePriority;
import ru.endlesscode.mimic.system.SystemType;

import java.lang.annotation.*;

/**
 * This annotation provides meta-information about factory to load it.
 * You must annotate with this all factories that you've added.
 *
 * @author Osip Fatkullin
 * @since 1.0
 */
@Documented
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Metadata {
    /**
     * Returns priority of factory
     *
     * @return Priority of factory
     */
    ServicePriority priority() default ServicePriority.Normal;

    /**
     * Returns classes that should exists for all systems working
     *
     * @implSpec
     * Example for SkillAPI:
     * system = {"com.sucy.skill.SkillAPI", "com.sucy.skill.api.player.PlayerData"}
     *
     * @return array of {@code String} with class names
     */
    String[] classes() default {};
}
