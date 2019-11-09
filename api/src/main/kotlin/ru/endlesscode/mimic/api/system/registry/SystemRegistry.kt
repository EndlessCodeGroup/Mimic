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
 * This is class responsible for accounting all system hooks.
 *
 * Systems - it is classes that directly extends PlayerSystem. Subsystems - it
 * is concrete implementations of systems.
 *
 * Recommended to use a singleton pattern when implement this class.
 */
interface SystemRegistry {

    /**
     * Registers hook of subsystem that given as class. Use registering with instance instead,
     * if you can. Because this method is slightly slower.
     *
     * @param SystemT        System type
     * @param subsystemClass Class of the subsystem
     * @return `true` if subsystem registered, or `false` if registration not needed
     * @throws SystemNotRegisteredException If registering failed
     */
    @JvmDefault
    fun <SystemT : PlayerSystem> registerSubsystem(subsystemClass: Class<out SystemT>): Boolean {
        return this.registerSubsystem(subsystemClass, null)
    }

    /**
     * Adds hook of subsystem if subsystem is can be added.
     *
     * If registering fails, will be thrown exception.
     *
     * @param SystemT        System type
     * @param subsystemClass Class of the subsystem
     * @param systemFactory  Subsystem factory (can be null)
     * @return `true` if subsystem registered, or `false` if registration not needed
     * @throws SystemNotRegisteredException If registering failed
     */
    @JvmDefault
    fun <SystemT : PlayerSystem> registerSubsystem(
        subsystemClass: Class<out SystemT>,
        systemFactory: SystemFactory<out SystemT>? = null
    ): Boolean {
        try {
            return this.tryToRegisterSubsystem(subsystemClass, systemFactory)
        } catch (e: IllegalArgumentException) {
            throw SystemNotRegisteredException("System didn't registered.", e)
        } catch (e: ClassCastException) {
            throw SystemNotRegisteredException("System didn't registered.", e)
        }
    }

    /**
     * Tries to register given factory.
     *
     * If [givenFactory] is null, it gets from class. You can override
     * [getSubsystemFactory] to change factory getting algorithm.
     *
     * @param SystemT        System type
     * @param subsystemClass Class of the subsystem
     * @param givenFactory   Instance of the subsystem factory (can be null)
     * @return `true` if subsystem registered, or `false` if registration not needed
     */
    @JvmDefault
    fun <SystemT : PlayerSystem> tryToRegisterSubsystem(
        subsystemClass: Class<out SystemT>,
        givenFactory: SystemFactory<out SystemT>?
    ): Boolean {
        val meta = SubsystemMetaAdapter.fromClass(subsystemClass)
        return if (meta.requiredClassesExists()) {
            val factory = givenFactory ?: this.getSubsystemFactory(subsystemClass)
            this.registerSystem(factory.javaClass, factory, meta.priority)
            true
        } else {
            false
        }
    }

    /**
     * Gets and returns factory from given class.
     *
     * Should not return null. Throw [IllegalArgumentException] instead.
     * In every subsystem needed to create factory
     *
     * @param SystemT        System type
     * @param subsystemClass Class of the subsystem
     * @return Factory of system
     * @throws IllegalArgumentException If factory not found
     */
    @JvmDefault
    fun <SystemT : PlayerSystem> getSubsystemFactory(subsystemClass: Class<out SystemT>): SystemFactory<out SystemT> {
        try {
            @Suppress("UNCHECKED_CAST")
            return subsystemClass.getField("FACTORY").get(null) as SystemFactory<out SystemT>
        } catch (e: ReflectiveOperationException) {
            throw IllegalArgumentException("Factory not found in given class", e)
        }
    }

    /**
     * Registers approved subsystem factory.
     *
     * @param SystemT          System type
     * @param FactoryT         Subsystem factory type
     * @param factoryClass     Class of the factory
     * @param subsystemFactory Concrete subsystem factory
     * @param priority         Subsystem priority
     */
    fun <SystemT : PlayerSystem, FactoryT : SystemFactory<out SystemT>> registerSystem(
        factoryClass: Class<FactoryT>,
        subsystemFactory: FactoryT,
        priority: SubsystemPriority
    )

    /**
     * Gets system factory by system class.
     *
     * Searches factory class inside system class and delegates it to [getFactory]
     *
     * @param SystemT     System type
     * @param systemClass System class
     * @return System factory or `null` if system factory not found in registry.
     */
    @JvmDefault
    fun <SystemT : PlayerSystem> getSystemFactory(systemClass: Class<SystemT>): SystemFactory<SystemT>? {
        val factoryClass = getFactoryClass(systemClass)
        return getFactory(factoryClass)
    }

    /**
     * Gets inner factory class from given system class.
     *
     * Each Player System must contains inner factory class.
     *
     * @param SystemT   System type
     * @param systemClass System class
     * @return Inner factory class from system class
     * @throws IllegalArgumentException If needed system factory not found in registry
     * @since 0.1
     */
    @JvmDefault
    fun <SystemT : PlayerSystem> getFactoryClass(systemClass: Class<SystemT>): Class<SystemFactory<SystemT>> {
        val innerClasses = systemClass.declaredClasses
        val factoryClass = requireNotNull(innerClasses.find { it.superclass == SystemFactory::class.java }) {
            "Given class not contains any System Factory"
        }

        @Suppress("UNCHECKED_CAST")
        return factoryClass as Class<SystemFactory<SystemT>>
    }

    /**
     * Gets system factory by factory class.
     *
     * @param SystemT      System type
     * @param factoryClass Factory class
     * @return System factory or `null` if system factory not found.
     */
    fun <SystemT : PlayerSystem> getFactory(factoryClass: Class<out SystemFactory<SystemT>>): SystemFactory<SystemT>?

    /**
     * Unregisters all subsystems.
     *
     * Use it before plugin disabling
     */
    fun unregisterAllSubsystems()

    /**
     * Unregister specified subsystem.
     *
     * @param SubsystemT     Subsystem type
     * @param subsystemClass Subsystem class
     */
    @JvmDefault
    fun <SubsystemT : PlayerSystem> unregisterSubsystem(subsystemClass: Class<SubsystemT>) {
        val factory = this.getSubsystemFactory(subsystemClass)
        unregisterFactory(factory)
    }

    /**
     * Unregister specified factory.
     *
     * @param SystemT System type
     * @param factory The factory
     */
    fun <SystemT : PlayerSystem> unregisterFactory(factory: SystemFactory<out SystemT>)
}
