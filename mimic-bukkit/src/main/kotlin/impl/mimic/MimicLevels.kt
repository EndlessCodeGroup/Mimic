// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mimic

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.ServiceEntry
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.call
import ru.endlesscode.mimic.level.LevelProgress
import ru.endlesscode.mimic.level.LevelProvider
import ru.endlesscode.mimic.level.LevelTrack
import ru.endlesscode.mimic.level.Levels

internal typealias LevelProviderEntry = ServiceEntry<LevelProvider>

/**
 * Levels combining tracks of all level providers, using provider IDs as namespaces: `mining` -> `auraskills:mining`.
 *
 * Deprecated level systems are registered here as providers of the single track `level`, see `MimicImpl.registerLevelSystem`.
 * The primary track is taken from the provider with [preferredProviderId].
 * If it isn't set, the primary track is taken from the first provider in order of priority that has one for the player.
 */
internal class MimicLevels(
    levelProviders: ServiceRegistry<LevelProvider>,
    preferredProviderId: () -> String,
) : Levels {

    val providers = NamespacedProviders(levelProviders, "primary track", preferredProviderId)

    override fun getAllTracks(): List<LevelTrack> =
        providers.enabled.flatMap { provider -> provider.getAllTracks() }

    override fun getTracks(player: Player): List<LevelTrack> {
        if (!player.isOnline) return emptyList()
        return providers.enabled.flatMap { provider -> provider.validTracks(player) }
    }

    override fun getPrimaryTrack(player: Player): LevelTrack? {
        if (!player.isOnline) return null
        return providers.selectPrimary { it.primaryTrack(player) }
    }

    override fun getProgress(player: Player, trackId: Key): LevelProgress? {
        if (!player.isOnline) return null
        val provider = findProvider(trackId) ?: return null
        return provider.call(default = null) { getProgress(player, trackId.value()) }
    }

    override fun hasLevel(player: Player, trackId: Key, level: Int): Boolean {
        val progress = getProgress(player, trackId) ?: return false
        return progress.level >= level
    }

    override fun setLevel(player: Player, trackId: Key, level: Int): Boolean {
        if (!player.isOnline) return false
        val provider = findProvider(trackId) ?: return false
        val progress = provider.call(default = null) { getProgress(player, trackId.value()) } ?: return false
        var clampedLevel = level.coerceAtLeast(progress.minLevel)
        if (progress.hasMaxLevel()) clampedLevel = clampedLevel.coerceAtMost(progress.maxLevel)
        return provider.call(default = false) { setLevel(player, trackId.value(), clampedLevel) }
    }

    override fun giveExp(player: Player, trackId: Key, amount: Double): Boolean {
        requireValidAmount(amount)
        if (!player.isOnline) return false
        val provider = findProvider(trackId) ?: return false
        return provider.call(default = false) { giveExp(player, trackId.value(), amount) }
    }

    override fun takeExp(player: Player, trackId: Key, amount: Double): Boolean {
        requireValidAmount(amount)
        if (!player.isOnline) return false
        val provider = findProvider(trackId) ?: return false
        return provider.call(default = false) { takeExp(player, trackId.value(), amount) }
    }

    private fun LevelProviderEntry.primaryTrack(player: Player): LevelTrack? {
        val track = call(default = null) { getPrimaryTrack(player) } ?: return null
        return if (isValidPath(track.id, TRACK)) levelTrack(track) else null
    }

    private fun findProvider(trackId: Key): LevelProviderEntry? =
        providers.find(trackId, TRACK) { path -> call(default = false) { allTracks.any { it.id == path } } }

    private fun requireValidAmount(amount: Double) {
        require(amount >= 0 && amount.isFinite()) { "'amount' should be non-negative and finite, but it was $amount" }
    }

    // Tracks are converted inside call, so a provider returning null tracks from Java is skipped
    private fun LevelProviderEntry.getAllTracks(): List<LevelTrack> =
        call(default = emptyList()) { levelTracks(allTracks) }
    private fun LevelProviderEntry.validTracks(player: Player): List<LevelTrack> =
        call(default = emptyList()) { levelTracks(getTracks(player)) }

    /** Returns the given [tracks] of this provider, skipping tracks with invalid or duplicated IDs. */
    private fun LevelProviderEntry.levelTracks(tracks: Collection<LevelProvider.Track>): List<LevelTrack> =
        validValues(tracks, TRACK) { it.id }.map { levelTrack(it) }

    private fun LevelProviderEntry.levelTrack(track: LevelProvider.Track): LevelTrack =
        MimicLevelTrack(key(track.id), track.name)

    private companion object {
        const val TRACK = "track"
    }
}

private class MimicLevelTrack(
    private val key: Key,
    private val name: Component,
) : LevelTrack {
    override fun key(): Key = key
    override fun name(): Component = name
    override fun equals(other: Any?): Boolean = other is LevelTrack && key == other.key()
    override fun hashCode(): Int = key.hashCode()
    override fun toString(): String = "LevelTrack(${key.asString()})"
}

/** IDs of all registered level providers, including bridged level systems, the values of `level-provider` config option. */
internal fun Mimic.getLevelProviderIds(): Set<String> = (levels as MimicLevels).providers.allIds

/** ID of the provider of the primary track. */
internal fun Mimic.getPrimaryLevelProviderId(): String? = (levels as MimicLevels).providers.primary?.service?.id
