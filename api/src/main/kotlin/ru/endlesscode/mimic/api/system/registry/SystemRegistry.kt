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
 * Class responsible for accounting all system hooks.
 *
 * Systems - it is classes that directly extends PlayerSystem. Subsystems - it
 * is concrete implementations of systems.
 *
 * Recommended to use a singleton pattern when implement this class.
 */
abstract class SystemRegistry {

    /**
     * Adds hook of subsystem if subsystem is can be added.
     *
     * If registering fails, will be thrown exception.
     *
     * @param subsystemClass Class of the subsystem
     * @param systemFactory  Subsystem factory (can be null)
     * @return `true` if subsystem registered, or `false` if registration not needed
     * @throws SystemNotRegisteredException If registering failed
     */
    @JvmOverloads
    fun <SubsystemT : PlayerSystem> registerSubsystem(
        subsystemClass: Class<SubsystemT>,
        systemFactory: SystemFactory<SubsystemT>? = null
    ): Boolean {
        try {
            return this.tryToRegisterSubsystem(subsystemClass, systemFactory)
        } catch (e: IllegalArgumentException) {
            throw SystemNotRegisteredException("System didn't registered.", e)
        } catch (e: ClassCastException) {
            throw SystemNotRegisteredException("System didn't registered.", e)
        }
    }

    private fun <SubsystemT : PlayerSystem> tryToRegisterSubsystem(
        subsystemClass: Class<SubsystemT>,
        givenFactory: SystemFactory<SubsystemT>?
    ): Boolean {
        val meta = SubsystemMetaAdapter.fromClass(subsystemClass)
        return if (meta.requiredClassesExists()) {
            val factory = givenFactory ?: subsystemClass.getSubsystemFactory()
            this.registerFactory(factory.javaClass.getRootFactoryClass(), factory, meta.priority)
            true
        } else {
            false
        }
    }

    private fun <SubsystemT : PlayerSystem> Class<SubsystemT>.getSubsystemFactory(): SystemFactory<SubsystemT> {
        try {
            @Suppress("UNCHECKED_CAST")
            return getField("FACTORY").get(null) as SystemFactory<SubsystemT>
        } catch (e: ReflectiveOperationException) {
            throw IllegalArgumentException("Factory not found in given class", e)
        }
    }

    /**
     * Registers approved subsystem factory.
     *
     * @param FactoryT         Subsystem factory type
     * @param factoryClass     Class of the factory
     * @param subsystemFactory Concrete subsystem factory
     * @param priority         Subsystem priority
     */
    abstract fun <FactoryT : SystemFactory<*>> registerFactory(
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
    fun <SystemT : PlayerSystem> getSystemFactory(systemClass: Class<SystemT>): SystemFactory<SystemT>? {
        val factoryClass = systemClass.getFactoryClass()
        return getFactory(factoryClass)
    }

    private fun <SystemT : PlayerSystem> Class<SystemT>.getFactoryClass(): Class<SystemFactory<SystemT>> {
        val factoryClass = declaredClasses.find(SystemFactory::class.java::isAssignableFrom)
        requireNotNull(factoryClass) { "Given class not contains any System Factory" }

        @Suppress("UNCHECKED_CAST")
        return (factoryClass as Class<out SystemFactory<SystemT>>).getRootFactoryClass()
    }

    private fun <SystemT : PlayerSystem> Class<out SystemFactory<SystemT>>.getRootFactoryClass(): Class<SystemFactory<SystemT>> {
        var factoryClass: Class<*> = this
        while (factoryClass.superclass != SystemFactory::class.java) {
            factoryClass = factoryClass.superclass
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
    abstract fun <SystemT : PlayerSystem> getFactory(
        factoryClass: Class<out SystemFactory<SystemT>>
    ): SystemFactory<SystemT>?

    /** Unregisters all subsystems. Use it before plugin disabling. */
    abstract fun unregisterAllSubsystems()

    /** Unregister subsystem with specified type. */
    fun <SubsystemT : PlayerSystem> unregisterSubsystem(subsystemClass: Class<SubsystemT>) {
        val factory = subsystemClass.getSubsystemFactory()
        unregisterFactory(factory)
    }

    /** Unregister specified [factory]. */
    abstract fun <SubsystemT : PlayerSystem> unregisterFactory(factory: SystemFactory<out SubsystemT>)
}
