// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.level

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/**
 * Levels of players from all RPG plugins supported by Mimic. Get it via [Mimic.getLevels][ru.endlesscode.mimic.Mimic.getLevels].
 * To make your own plugin's levels available here, implement [LevelProvider] instead.
 *
 * A **track** is one thing a player levels up in: a class, a skill, a profession, a job, or the single global level.
 * A player may have many tracks from several plugins at once.
 *
 * It replaces the deprecated `LevelSystem` API, which supports only a single level per player.
 *
 * ### Quick start
 *
 * If you need just "the player's level", use the primary track. Server owners choose its plugin in Mimic config:
 * ```java
 * LevelTrack track = levels.getPrimaryTrack(player);
 * if (track == null) return; // The selected plugin has no primary track
 *
 * // Check level quickly
 * if (levels.hasLevel(player, track.key(), 10)) { ... }
 *
 * // Or get a progress object containing info about level and experience
 * LevelProgress progress = levels.getProgress(player, track.key());
 * if (progress != null && progress.getLevel() >= 10) { ... }
 * ```
 *
 * ### Track IDs
 *
 * Tracks are identified by [Key], which consists of a namespace and a path:
 * - The namespace is the ID of the plugin providing the track: `minecraft`, `auraskills`.
 * - The path is the track within that plugin: `level`, `mining`.
 *
 * For example, `minecraft` provides a single track `minecraft:level` - the vanilla level,
 * and a skills plugin may provide `auraskills:mining` and `auraskills:combat`.
 *
 * The namespace is required, since the same path in different plugins means a different level.
 * Note that `Key.key("mining")` turns into `minecraft:mining`,
 * so check track IDs read from configs against [getAllTracks] when your plugin loads.
 *
 * ### Rules
 *
 * - Works only with online players.
 * - `null` or `false` means "not possible right now": the track doesn't exist, the player doesn't have it,
 *   the plugin doesn't support this action, or the player's data isn't loaded yet.
 * - Don't cache results. Read them again each time you need them.
 * - Read from any thread. Change levels and experience only on the main thread (on Folia - on the player's thread).
 *
 * @since 0.10.0
 */
public interface Levels {

    /**
     * Returns all tracks of all providers, regardless of players. Use it for discovery and config validation.
     * Tracks are sorted by provider priority, then in the order the provider returns them.
     */
    public fun getAllTracks(): List<LevelTrack>

    /**
     * Returns tracks the [player] currently has.
     * Tracks are sorted by provider priority, then in the order the provider returns them.
     */
    public fun getTracks(player: Player): List<LevelTrack>

    /**
     * Returns the track that best represents the [player]'s overall level, or `null` if there is no such track.
     *
     * Server owners choose the plugin for it with the `level-provider` option in Mimic config.
     * If the option isn't set, or the plugin isn't installed, Mimic asks plugins one by one,
     * from the highest priority, and returns the first primary track it gets.
     */
    public fun getPrimaryTrack(player: Player): LevelTrack?

    /** Returns progress on the [trackId], or `null` if the [player] doesn't have the track or their data isn't loaded. */
    public fun getProgress(player: Player, trackId: Key): LevelProgress?

    /** Returns `true` if the [player] has the track with [trackId] and their level on it is at least [level]. */
    public fun hasLevel(player: Player, trackId: Key, level: Int): Boolean

    /**
     * Sets [level] with 0 experience within the level on the [trackId].
     * The level is clamped to [LevelProgress.minLevel] and [LevelProgress.maxLevel].
     * Whether the player gets the plugin's level-up rewards (skill points, unlocks, messages) for skipped levels
     * depends on the plugin. Lowering the level may not take rewards back.
     *
     * @return `true` if the change was requested, `false` if it is unsupported or the player's data isn't loaded.
     *   `true` doesn't guarantee the change: another plugin may cancel it, and Mimic can't detect that.
     */
    public fun setLevel(player: Player, trackId: Key, level: Int): Boolean

    /**
     * Gives exactly [amount] experience on the [trackId], ignoring boosters, party sharing and other bonuses
     * the plugin applies to experience earned in-game. Some plugins always apply their boosters, so the player may get more.
     * The plugin's level-up rewards and messages still work as usual.
     *
     * @param amount Non-negative amount of experience.
     * @return `true` if the change was requested, `false` if it is unsupported or the player's data isn't loaded.
     *   `true` doesn't guarantee the change: another plugin may cancel it, and Mimic can't detect that.
     * @throws IllegalArgumentException if [amount] is negative or not finite.
     */
    public fun giveExp(player: Player, trackId: Key, amount: Double): Boolean

    /**
     * Takes experience [amount] on the [trackId], lowering levels down to [LevelProgress.minLevel] if needed.
     *
     * @param amount Non-negative amount of experience.
     * @return `true` if the change was requested, `false` if it is unsupported or the player's data isn't loaded.
     *   `true` doesn't guarantee the change: another plugin may cancel it, and Mimic can't detect that.
     * @throws IllegalArgumentException if [amount] is negative or not finite.
     */
    public fun takeExp(player: Player, trackId: Key, amount: Double): Boolean
}

/**
 * Level track available via [Levels]. Tracks are equal if their keys are equal.
 * @since 0.10.0
 */
public interface LevelTrack : Keyed {

    /** Full track ID, where namespace is the provider ID: `auraskills:mining`. */
    override fun key(): Key

    /** Display name of the track defined by the plugin. */
    public fun name(): Component
}
