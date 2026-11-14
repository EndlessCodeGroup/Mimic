// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.level

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

/**
 * Provider of the single primary track, for plugins with one global level.
 * Implement the methods without track ID, requests for other tracks return `null` or `false`.
 */
internal abstract class SingleTrackLevelProvider(
    trackId: String = DEFAULT_TRACK,
    name: Component = Component.text("Level"),
) : LevelProvider {

    private val track = LevelProvider.Track(trackId, name)

    final override val allTracks: Collection<LevelProvider.Track> = listOf(track)

    final override fun getTracks(player: Player): Collection<LevelProvider.Track> = allTracks
    final override fun getPrimaryTrack(player: Player): LevelProvider.Track = track

    final override fun getProgress(player: Player, trackId: String): LevelProgress? =
        if (trackId == track.id) getProgress(player) else null

    final override fun setLevel(player: Player, trackId: String, level: Int): Boolean =
        trackId == track.id && setLevel(player, level)

    final override fun giveExp(player: Player, trackId: String, amount: Double): Boolean =
        trackId == track.id && giveExp(player, amount)

    final override fun takeExp(player: Player, trackId: String, amount: Double): Boolean =
        trackId == track.id && takeExp(player, amount)

    abstract fun getProgress(player: Player): LevelProgress?
    open fun setLevel(player: Player, level: Int): Boolean = false
    open fun giveExp(player: Player, amount: Double): Boolean = false
    open fun takeExp(player: Player, amount: Double): Boolean = false

    companion object {
        const val DEFAULT_TRACK = "level"
    }
}
