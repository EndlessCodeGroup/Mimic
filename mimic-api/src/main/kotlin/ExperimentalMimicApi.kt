/*
 * This file is part of Mimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
 *
 * Mimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Mimic.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic

import kotlin.annotation.AnnotationTarget.*

/**
 * This annotation marks that the API is considered experimental.
 * Such API may (or may not) be changed, or it may be removed in any further release.
 */
@Target(CLASS, FUNCTION, PROPERTY)
@MustBeDocumented
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
public annotation class ExperimentalMimicApi
