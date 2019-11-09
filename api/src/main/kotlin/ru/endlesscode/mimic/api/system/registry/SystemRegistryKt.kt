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

package ru.endlesscode.mimic.api.system.registry

import ru.endlesscode.mimic.api.system.PlayerSystem
import ru.endlesscode.mimic.api.system.SystemFactory

/**
 * Adds hook of subsystem if subsystem is can be added.
 *
 * @return `true` if subsystem registered, or `false` if registration not needed
 * @throws SystemNotRegisteredException If registering failed
 */
inline fun <reified SubsystemT : PlayerSystem> SystemRegistry.registerSubsystem(
    systemFactory: SystemFactory<SubsystemT>? = null
): Boolean {
    return this.registerSubsystem(SubsystemT::class.java, systemFactory)
}

/**
 * Gets system factory by system class.
 *
 * @param SystemT     System type
 * @return System factory or `null` if system factory not found in registry.
 */
inline fun <reified SystemT : PlayerSystem> SystemRegistry.getSystemFactory(): SystemFactory<SystemT>? {
    return this.getSystemFactory(SystemT::class.java)
}

/** Unregister subsystem with specified type. */
inline fun <reified SubsystemT : PlayerSystem> SystemRegistry.unregisterSubsystem() {
    this.unregisterSubsystem(SubsystemT::class.java)
}
