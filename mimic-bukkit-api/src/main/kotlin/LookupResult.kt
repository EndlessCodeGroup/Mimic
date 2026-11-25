// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic

import net.kyori.adventure.key.Key

/**
 * Result of a lenient ID lookup, like [Levels.lookupTrack][ru.endlesscode.mimic.level.Levels.lookupTrack].
 *
 * If you only need an ID that works right now, take [foundId]:
 * ```java
 * String configuredTrack = config.getString("track", ""); // "Woodcutting"
 * Key trackId = levels.lookupTrack(configuredTrack).getFoundId(); // mcmmo:woodcutting
 * if (trackId == null) return;
 * ```
 * To tell a server owner what is wrong with their config, check the [status].
 *
 * The result is a snapshot. Plugins may load their content after the server start and change it on reload,
 * so the same value may give another result later. Don't look up IDs in `onLoad`.
 *
 * @since 0.12.0
 */
public interface LookupResult {

    /** What the lookup ended with. */
    public val status: LookupStatus

    /**
     * Existing IDs matching the value: one for [LookupStatus.FOUND], several for [LookupStatus.AMBIGUOUS],
     * otherwise empty.
     */
    public val ids: List<Key>

    /** The ID if the [status] is [LookupStatus.FOUND], otherwise `null`. */
    public val foundId: Key?
        get() = if (status == LookupStatus.FOUND) ids.single() else null
}

/**
 * Status of [LookupResult].
 * @since 0.12.0
 */
public enum class LookupStatus {

    /** The ID is found. Use [LookupResult.foundId]. */
    FOUND,

    /**
     * Nothing matches the value now: it is a typo, or the plugin hasn't loaded it yet.
     * Warn the server owner, and look up the value again when you use it.
     */
    NOT_FOUND,

    /**
     * The value has no namespace and several plugins have this name, see [LookupResult.ids].
     * Ask the server owner to add the namespace.
     */
    AMBIGUOUS,

    /**
     * The value has a namespace, and no installed plugin owns it: the plugin is missing, or the namespace is misspelled.
     * Skip the value if it is optional, otherwise report it.
     */
    NO_PROVIDER,
}
