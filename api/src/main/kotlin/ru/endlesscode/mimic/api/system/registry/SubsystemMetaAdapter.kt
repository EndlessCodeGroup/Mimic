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

/**
 * Adapter to work with systems [Subsystem].
 *
 * @see Subsystem
 *
 * @param SubsystemT Subsystem type
 * @param meta The metadata
 * @author Osip Fatkullin
 * @since 0.1
 */
class SubsystemMetaAdapter<SubsystemT : PlayerSystem> private constructor(private val meta: Subsystem) {

    companion object {
        /**
         * Gets metadata from class annotation. If annotation not exists - throws exception.
         *
         * @see Subsystem
         *
         * @param SubsystemT subsystem type
         * @param theClass subsystem class
         * @return [SubsystemMetaAdapter] if metadata exists, otherwise throws exception
         * @throws IllegalArgumentException If [Subsystem] not exists
         */
        @JvmStatic
        fun <SubsystemT : PlayerSystem> fromClass(theClass: Class<out SubsystemT>): SubsystemMetaAdapter<SubsystemT> {
            val meta = requireNotNull(theClass.getAnnotation(Subsystem::class.java)) {
                "Class not contains subsystem annotations."
            }

            return SubsystemMetaAdapter(meta)
        }
    }

    /**
     * Returns system priority.
     *
     * @return System priority
     */
    val priority: SubsystemPriority get() = meta.priority

    /**
     * Checks existence of all required classes.
     *
     * @return true if all classes exists, otherwise false
     */
    fun requiredClassesExists(): Boolean {
        val classNames = meta.classes

        return try {
            classNames.forEach { Class.forName(it) }
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
