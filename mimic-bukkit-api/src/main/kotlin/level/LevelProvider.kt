// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.level

import net.kyori.adventure.key.KeyPattern
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.MimicService

/**
 * Implement it to make your plugin's levels available via [Levels].
 * Register it in `onLoad` via [Mimic.registerLevelProvider][ru.endlesscode.mimic.Mimic.registerLevelProvider].
 * To read or change levels of players, use [Levels] instead.
 *
 * A **track** is one thing a player levels up in: a class, a skill, a profession, a job, or the single global level.
 * Your plugin may have one track or many.
 *
 * It replaces the deprecated `LevelSystem` API, which supports only a single level per player.
 *
 * ### What to implement
 *
 * - Required: [allTracks], [getTracks], [getPrimaryTrack] and [getProgress].
 * - Optional: [setLevel], [giveExp] and [takeExp]. Skip the ones your plugin can't do, they return `false` by default.
 *
 * ### Track IDs
 *
 * Mimic adds your provider [id] as the namespace, so return IDs without it: `mining` becomes `myplugin:mining`.
 * Don't change [id] later, it breaks consumers' configs.
 *
 * If your plugin has its own namespaced IDs, convert them to paths:
 * - Drop your plugin's own namespace: `auraskills:mining` -> `mining`.
 * - Keep other namespaces as the first path part: `myaddon:fishing` -> `myaddon/fishing`.
 *
 * Other rules:
 * - Allowed characters: `a-z`, `0-9`, `.`, `_`, `-` and `/`. Tracks with other characters are ignored.
 * - Group related tracks with the first part of the path: `class/mage`, `profession/mining`.
 * - To convert other IDs, lowercase them and replace other characters with `_`.
 *   Keep a map back to the original IDs.
 * - If two IDs become the same (`Mining` and `mining`), log a warning and keep one of them.
 *
 * ### Rules
 *
 * - Only online players are passed to the provider.
 * - Never throw. If the player's data isn't loaded yet, return `null` from reads and `false` from writes.
 * - Don't change data until the player's data is loaded, even if your plugin's API allows it.
 * - Reads may be called from any thread. If your plugin can't read data there, return `null` instead of throwing.
 * - Writes are expected on the main thread (on Folia - on the player's thread), but Mimic doesn't enforce it.
 *
 * Mimic does the rest: selects the provider by the track ID namespace, checks amounts,
 * keeps levels within [LevelProgress.minLevel] and [LevelProgress.maxLevel], and catches errors thrown by the provider.
 *
 * @since 0.10.0
 */
public interface LevelProvider : MimicService {

    /**
     * All tracks this provider knows, regardless of players.
     * The list may change at runtime, so it is read on every request.
     */
    public val allTracks: Collection<Track>

    /** Returns tracks the [player] currently has. */
    public fun getTracks(player: Player): Collection<Track>

    /**
     * Returns the track that best represents the [player]'s overall level, or `null` if there is no such track.
     * It may be a read-only track, like a power level summing all skills.
     */
    public fun getPrimaryTrack(player: Player): Track?

    /** Returns progress on the [trackId], or `null` if the [player] doesn't have the track or their data isn't loaded. */
    public fun getProgress(player: Player, @KeyPattern.Value trackId: String): LevelProgress?

    /**
     * Sets [level] with 0 experience within the level on the [trackId].
     * Mimic clamps [level] to [LevelProgress.minLevel] and [LevelProgress.maxLevel] before calling this method.
     * If your plugin can give its level-up rewards for skipped levels, do it. Otherwise, just set the level.
     *
     * @return `true` if the change was requested, `false` if it is unsupported for this [player] and [trackId],
     *   or the player's data isn't loaded. Return `true` even if the plugin may cancel the change through its events.
     */
    public fun setLevel(player: Player, @KeyPattern.Value trackId: String, level: Int): Boolean = false

    /**
     * Gives exactly [amount] experience on the [trackId]: skip boosters, party sharing and other in-game bonuses
     * if your plugin's API allows it. Keep the plugin's level-up rewards and messages.
     *
     * @param amount Non-negative amount of experience.
     * @return `true` if the change was requested, `false` if it is unsupported for this [player] and [trackId],
     *   or the player's data isn't loaded. Return `true` even if the plugin may cancel the change through its events.
     */
    public fun giveExp(player: Player, @KeyPattern.Value trackId: String, amount: Double): Boolean = false

    /**
     * Takes experience [amount] on the [trackId], lowering levels down to [LevelProgress.minLevel] if needed.
     * Emulate it as compute-then-set if the plugin can't lower levels natively.
     *
     * @param amount Non-negative amount of experience.
     * @return `true` if the change was requested, `false` if it is unsupported for this [player] and [trackId],
     *   or the player's data isn't loaded. Return `true` even if the plugin may cancel the change through its events.
     */
    public fun takeExp(player: Player, @KeyPattern.Value trackId: String, amount: Double): Boolean = false

    /**
     * Track of a [LevelProvider]. Tracks are immutable, so they can be cached and reused.
     * @property id Track path, like `mining` or `profession/mining`.
     * @property name Display name of the track defined by the plugin.
     */
    public class Track(
        @param:KeyPattern.Value
        public val id: String,
        public val name: Component,
    )
}
