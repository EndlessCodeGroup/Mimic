// Copyright (c) 2017 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

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
