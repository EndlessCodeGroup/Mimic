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

import com.sun.istack.internal.NotNull;
import ru.endlesscode.mimic.system.PlayerSystem;

import java.util.logging.Logger;

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
    private final Logger log;

    /**
     * Constructor with logger initialization
     *
     * @param log Logger for messages
     */
    protected SystemRegistry(Logger log) {
        this.log = log;
    }

    /**
     * Registers subsystem that given as instance.
     *
     * @param <SubsystemT>  Subsystem type
     * @param subsystem     Instance of the subsystem
     */
    public <SubsystemT extends PlayerSystem> void addSubsystem(@NotNull SubsystemT subsystem) {
        this.addSubsystem(subsystem.getClass(), subsystem);
    }

    /**
     * Registers hook of subsystem that given as class.
     *
     * @param <SubsystemT>      Subsystem type
     * @param subsystemClass    Class of the subsystem
     */
    public <SubsystemT extends PlayerSystem> void addSubsystem(@NotNull Class<SubsystemT> subsystemClass) {
        this.addSubsystem(subsystemClass, null);
    }

    /**
     * Adds hook of subsystem if subsystem is can be added.
     *
     * @implNote
     * If registering fails, will be printed message to console.
     *
     * @param <SubsystemT>      Subsystem type
     * @param subsystemClass    Class of the subsystem
     * @param subsystem         Instance of the subsystem (can be {@code null})
     */
    protected <SubsystemT extends PlayerSystem> void addSubsystem(
            @NotNull Class<? extends SubsystemT> subsystemClass, SubsystemT subsystem) {
        try {
            this.tryToAddSubsystem(subsystemClass, subsystem);
        } catch (Exception e) {
            log.severe("[Hook] " + e.getMessage());
        }
    }

    /**
     * Tries to hook subsystem. If hook failed throws exception.
     *
     * @implNote
     * If {@code subsystem} is {@code null}, will be created new. You can override
     * {@link #createSubsystemInstance(Class)} to change instance creating algorithm.
     *
     * @param <SubsystemT>      Subsystem type
     * @param subsystemClass    Class of the subsystem
     * @param subsystem         Instance of the subsystem (can be {@code null})
     */
    protected <SubsystemT extends PlayerSystem> void tryToAddSubsystem(
            @NotNull Class<? extends SubsystemT> subsystemClass, SubsystemT subsystem) {
        MetadataAdapter meta = MetadataAdapter.getNotNullMeta(subsystemClass);
        if (!meta.requiredClassesExists()) {
            return;
        }

        if (subsystem == null) {
            subsystem = this.createSubsystemInstance(subsystemClass);
        }

        this.registerSystem(subsystem, meta);

        String status = subsystem.isEnabled() ? "Loaded" : "Waiting";
        log.info(String.format("[%s] %s found: %s", meta.getSystemName(), subsystem.getName(), status));
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
    @NotNull
    protected <SubsystemT extends PlayerSystem> SubsystemT createSubsystemInstance(@NotNull Class<? extends SubsystemT> subsystemClass) {
        try {
            return subsystemClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Instance from given class can't be created");
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
     * Use pattern Prototype to initialize new system objects. Subsystems must be
     * initializable (must contains init method).
     *
     * @param <SystemT>         System type
     * @param systemTypeClass   System type class
     * @param args              Arguments that needed to recognize player
     * @return System assigned to player
     */
    protected abstract <SystemT extends PlayerSystem> SystemT getSystem(@NotNull Class<SystemT> systemTypeClass,
                                                                     Object... args);

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
     * @param subsystemClass    Class of the subsystem
     */
    public abstract <SubsystemT extends PlayerSystem> void unregisterSubsystem(@NotNull Class<SubsystemT> subsystemClass);
}
