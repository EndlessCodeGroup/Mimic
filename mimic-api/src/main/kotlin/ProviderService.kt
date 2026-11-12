// Copyright (c) 2017 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic

/**
 * Service that provides [T].
 * Should be used if some service needs arguments for initialization.
 */
public interface ProviderService<out T : Any> : MimicService {

    /** Creates new [T] initialized with [arg]. */
    public fun get(arg: Any): T
}
