// Copyright (c) 2017 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic

/** This interface should be implemented by any service related to Mimic. */
public interface MimicService {

    /**
     * Returns `true` if the service is initialized may be used.
     * Default implementation always returns `true`.
     */
    public val isEnabled: Boolean
        get() = true

    /**
     * Returns the ID of the service. It should match regex `[a-z0-9._-]`.
     *
     * ID should be unique, so it is recommended to use the plugin name as ID.
     * Default implementation uses plugin name as an ID.
     */
    public val id: String
        get() = USE_PLUGIN_NAME_AS_ID

    public companion object {
        /**
         * Empty string means that plugin name will be used as a service ID.
         * It should be mapped on implementation wrapper layer.
         */
        public const val USE_PLUGIN_NAME_AS_ID: String = ""
    }
}
