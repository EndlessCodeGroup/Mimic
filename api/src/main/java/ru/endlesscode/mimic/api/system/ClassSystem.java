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

package ru.endlesscode.mimic.api.system;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * System that provides methods to work with players class systems.
 *
 * <p>Before implementing run an eye over all default method implementations
 * and override all methods that works not properly for your case.</p>
 *
 * <p>See {@link PlayerSystem} To read more about implementation.</p>
 *
 * @author Osip Fatkullin
 * @since 0.1
 */
public abstract class ClassSystem implements PlayerSystem {
    /**
     * Checks player has any class.
     *
     * @return {@code true} if player has any class
     */
    public boolean hasClass() {
        return !this.getPrimaryClass().isEmpty();
    }

    /**
     * Checks player has one of required classes.
     *
     * @param requiredClasses List of required classes
     * @return {@code true} if player has one of required class
     */
    public boolean hasOneOfRequiredClasses(@NotNull List<String> requiredClasses) {
        for (String requiredClass : requiredClasses) {
            if (this.hasRequiredClass(requiredClass)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks player has all required classes.
     *
     * @param requiredClasses List of required classes
     * @return {@code true} if player has all required class
     */
    public boolean hasAllRequiredClasses(@NotNull List<String> requiredClasses) {
        for (String requiredClass : requiredClasses) {
            if (!this.hasRequiredClass(requiredClass)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks player has required class.
     *
     * @param requiredClass Required class name
     * @return {@code true} if player has required class
     */
    public boolean hasRequiredClass(@NotNull String requiredClass) {
        return this.getClasses().contains(requiredClass);
    }

    /**
     * Gets primary class for player.
     *
     * @apiNote
     * Class is called "primary" because some systems can support many system
     * for one player.
     *
     * @implSpec
     * Method shouldn't return {@code null}, but can return empty {@code String}
     *
     * @return Primary class name
     */
    public @NotNull String getPrimaryClass() {
        List<String> playerClasses = this.getClasses();
        return playerClasses.isEmpty() ? "" : playerClasses.get(0);
    }

    /**
     * Gets {@code Lost} of player system.
     *
     * @apiNote
     * This method actual for systems which support many system for one player.
     * If system not support - it just return {@code List} with one element.
     *
     * @implSpec
     * Method shouldn't return {@code null}, but can return empty {@code List}.
     * Also must not contain null objects.
     *
     * @return {@code List} of player system names
     * @throws IllegalStateException If player-related object not exists.
     */
    public abstract @NotNull List<String> getClasses();

    /**
     * Factory of class systems.
     */
    public static class Factory extends SystemFactory<ClassSystem> {
        public Factory(Function<Object, ? extends ClassSystem> constructor) {
            super(constructor);
        }
    }
}
