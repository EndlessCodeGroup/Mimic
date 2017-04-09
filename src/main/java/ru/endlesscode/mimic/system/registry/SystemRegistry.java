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

package ru.endlesscode.mimic.system.registry;

import org.jetbrains.annotations.NotNull;
import ru.endlesscode.mimic.system.PlayerSystem;

/**
 * This is class responsible for accounting all system hooks.
 *
 * @apiNote
 * Systems - it is classes that directly extends PlayerSystem. Subsystems - it
 * is concrete implementations of systems.
 *
 * @implNote
 * Recommended to use a singleton pattern when implement this class.
 *
 * @author Osip Fatkullin
 * @since 1.0
 */
public abstract class SystemRegistry {
    /**
     * Registers subsystem that given as instance.
     *
     * @param <SubsystemT>  Subsystem type
     * @param subsystem     Instance of the subsystem
     * @throws RegistryOperationException If registering failed
     */
    public <SubsystemT extends PlayerSystem> void registerSubsystem(@NotNull SubsystemT subsystem)
            throws RegistryOperationException {
        this.registerSubsystem(subsystem.getClass(), subsystem);
    }

    /**
     * Registers hook of subsystem that given as class.
     *
     * @param <SubsystemT>      Subsystem type
     * @param subsystemClass    Class of the subsystem
     * @throws RegistryOperationException If registering failed
     */
    public <SubsystemT extends PlayerSystem> void registerSubsystem(@NotNull Class<SubsystemT> subsystemClass)
            throws RegistryOperationException {
        this.registerSubsystem(subsystemClass, null);
    }

    /**
     * Adds hook of subsystem if subsystem is can be added.
     *
     * @implNote
     * If registering fails, will be thrown exception.
     *
     * @param <SubsystemT>      Subsystem type
     * @param subsystemClass    Class of the subsystem
     * @param subsystem         Instance of the subsystem (can be {@code null})
     * @throws RegistryOperationException If registering failed
     */
    protected <SubsystemT extends PlayerSystem> void registerSubsystem(
            @NotNull Class<? extends SubsystemT> subsystemClass, SubsystemT subsystem)
            throws RegistryOperationException {
        try {
            this.tryToRegisterSubsystem(subsystemClass, subsystem);
        } catch (IllegalArgumentException e) {
            throw new SystemNotRegisteredException("System didn't registered.", e);
        }
    }

    /**
     * Tries to hook givenSubsystem. If hook failed throws exception.
     *
     * @implNote
     * If {@code givenSubsystem} is {@code null}, will be created new. You can override
     * {@link #createSubsystemInstance(Class)} to change instance creating algorithm.
     *
     * @param <SubsystemT>      Subsystem type
     * @param subsystemClass    Class of the subsystem
     * @param givenSubsystem    Instance of the subsystem (can be {@code null})
     * @throws SystemNotNeededException If some requirements aren't met
     */
    protected <SubsystemT extends PlayerSystem> void tryToRegisterSubsystem(
            @NotNull Class<? extends SubsystemT> subsystemClass,
            SubsystemT givenSubsystem)
            throws SystemNotNeededException {
        MetadataAdapter meta = MetadataAdapter.getNotNullMeta(subsystemClass);
        if (!meta.requiredClassesExists()) {
            throw new SystemNotNeededException(String.format("Required classes for '%s' not found.", subsystemClass.getSimpleName()));
        }

        SubsystemT subsystem = givenSubsystem == null ? this.createSubsystemInstance(subsystemClass) : givenSubsystem;
        this.registerSystem(subsystem, meta);
    }

    /**
     * Creates instance of subsystem from given class.
     *
     * @implSpec
     * Should not return {@code null}. Throw {@code IllegalArgumentException} instead.
     *
     * @param <SubsystemT>      Subsystem type
     * @param subsystemClass    Class of the subsystem
     * @return Created subsystem instance
     * @throws IllegalArgumentException If instance can't be created
     */
    protected @NotNull <SubsystemT extends PlayerSystem> SubsystemT createSubsystemInstance(
            @NotNull Class<? extends SubsystemT> subsystemClass) {
        try {
            return subsystemClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Instance from given class can't be created", e);
        }
    }

    /**
     * Registers approved subsystem.
     *
     * @param <SubsystemT>  Subsystem type
     * @param subsystem     Instance of the subsystem
     * @param meta          Subsystem metadata
     */
    protected abstract <SubsystemT extends PlayerSystem> void registerSystem(
            @NotNull SubsystemT subsystem,
            @NotNull MetadataAdapter meta);

    /**
     * Gets system assigned to specified
     *
     * @implNote
     * Use pattern Prototype to initialize new system objects. All subsystems
     * contains method {@link PlayerSystem#initializedCopy(Object...)} for this.
     *
     * @implSpec
     * Never return {@code null}. Throw exception instead.
     *
     * @param <SystemT>         System type
     * @param systemTypeClass   System type class
     * @param args              Arguments that needed to initialize system
     * @return System assigned to player
     * @throws SystemNotRegisteredException If needed system not registered
     */
    protected abstract @NotNull <SystemT extends PlayerSystem> SystemT getSystem(
            @NotNull Class<SystemT> systemTypeClass,
            Object... args) throws SystemNotRegisteredException;

    /**
     * Unregisters all subsystems
     *
     * @apiNote Use it before plugin disabling
     */
    public abstract void unregisterAllSubsystems();

    /**
     * Unregister specified subsystem
     *
     * @param <SubsystemT>      Subsystem type
     * @param subsystem         The subsystem
     */
    public abstract <SubsystemT extends PlayerSystem> void unregisterSubsystem(
            @NotNull SubsystemT subsystem);
}
