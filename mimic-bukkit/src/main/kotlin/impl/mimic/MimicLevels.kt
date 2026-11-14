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
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.internal.authorsMention
import ru.endlesscode.mimic.level.LevelProgress
import ru.endlesscode.mimic.level.LevelProvider
import ru.endlesscode.mimic.level.LevelTrack
import ru.endlesscode.mimic.level.Levels
import java.util.concurrent.ConcurrentHashMap

internal typealias LevelProviderEntry = ServiceEntry<LevelProvider>

/**
 * Levels combining tracks of all level providers, using provider IDs as namespaces: `mining` -> `auraskills:mining`.
 *
 * Deprecated level systems are registered here as providers of the single track `level`, see `MimicImpl.registerLevelSystem`.
 * The primary track is taken from the provider with [preferredProviderId].
 * If it isn't set, the primary track is taken from the first provider in order of priority that has one for the player.
 */
internal class MimicLevels(
    private val levelProviders: ServiceRegistry<LevelProvider>,
    private val preferredProviderId: () -> String,
) : Levels {

    /** Enabled providers in order of priority, Mimic default implementations last. */
    val providers: List<LevelProviderEntry>
        get() = levelProviders.enabledEntries

    /** IDs of all registered providers, including disabled ones. */
    val allProviderIds: Set<String>
        get() = levelProviders.entries.mapTo(LinkedHashSet()) { it.service.id }

    /**
     * The preferred provider, or the provider with the highest priority, shown as selected in the config.
     * It is only an approximation: without a preference, [getPrimaryTrack] skips providers without a primary track.
     * It is `null` only while Mimic isn't enabled, before its `onEnable` and after its `onDisable`,
     * because [providers] skips providers of disabled plugins, including Mimic's own `minecraft` provider.
     */
    val primaryProvider: LevelProviderEntry?
        get() {
            val providers = providers
            return preferredProvider(providers) ?: providers.firstOrNull()
        }

    /** Keys of the warnings already logged, to log each warning once. */
    private val loggedWarnings = ConcurrentHashMap.newKeySet<String>()

    override fun getAllTracks(): List<LevelTrack> = providers.flatMap { provider ->
        provider.call(emptyList()) { provider.levelTracks(allTracks) }
    }

    override fun getTracks(player: Player): List<LevelTrack> {
        if (!player.isOnline) return emptyList()
        return providers.flatMap { provider ->
            provider.call(emptyList()) { provider.levelTracks(getTracks(player)) }
        }
    }

    override fun getPrimaryTrack(player: Player): LevelTrack? {
        if (!player.isOnline) return null
        val providers = providers
        val preferredProvider = preferredProvider(providers)
        if (preferredProvider != null) return preferredProvider.primaryTrack(player)
        return providers.firstNotNullOfOrNull { it.primaryTrack(player) }
    }

    override fun getProgress(player: Player, trackId: Key): LevelProgress? {
        if (!player.isOnline) return null
        val provider = findProvider(trackId) ?: return null
        return provider.call(null) { getProgress(player, trackId.value()) }
    }

    override fun hasLevel(player: Player, trackId: Key, level: Int): Boolean {
        val progress = getProgress(player, trackId) ?: return false
        return progress.level >= level
    }

    override fun setLevel(player: Player, trackId: Key, level: Int): Boolean {
        if (!player.isOnline) return false
        val provider = findProvider(trackId) ?: return false
        val progress = provider.call(null) { getProgress(player, trackId.value()) } ?: return false
        var clampedLevel = level.coerceAtLeast(progress.minLevel)
        if (progress.hasMaxLevel()) clampedLevel = clampedLevel.coerceAtMost(progress.maxLevel)
        return provider.call(false) { setLevel(player, trackId.value(), clampedLevel) }
    }

    override fun giveExp(player: Player, trackId: Key, amount: Double): Boolean {
        requireValidAmount(amount)
        if (!player.isOnline) return false
        val provider = findProvider(trackId) ?: return false
        return provider.call(false) { giveExp(player, trackId.value(), amount) }
    }

    override fun takeExp(player: Player, trackId: Key, amount: Double): Boolean {
        requireValidAmount(amount)
        if (!player.isOnline) return false
        val provider = findProvider(trackId) ?: return false
        return provider.call(false) { takeExp(player, trackId.value(), amount) }
    }

    /** Returns the provider selected in the config, or `null` if it isn't selected or isn't available. */
    private fun preferredProvider(providers: List<LevelProviderEntry>): LevelProviderEntry? {
        val preferredId = preferredProviderId()
        if (preferredId.isEmpty()) return null
        val provider = providers.find { it.service.id == preferredId }
        if (provider == null) {
            warnOnce("unknown-preferred-provider:$preferredId") {
                "Level provider '$preferredId' set in config isn't available, " +
                    "the primary track is taken from other providers."
            }
        }
        return provider
    }

    private fun LevelProviderEntry.primaryTrack(player: Player): LevelTrack? {
        val track = call(null) { getPrimaryTrack(player) } ?: return null
        return if (isValidPath(track.id)) levelTrack(track) else null
    }

    private fun findProvider(trackId: Key): LevelProviderEntry? {
        val provider = providers.find { it.service.id == trackId.namespace() }
        if (trackId.namespace() == Key.MINECRAFT_NAMESPACE) warnIfUnknownMinecraftTrack(trackId, provider)
        return provider
    }

    /**
     * `Key.key(String)` adds the `minecraft:` namespace, so an unknown `minecraft:` track likely misses its namespace.
     */
    private fun warnIfUnknownMinecraftTrack(trackId: Key, provider: LevelProviderEntry?) {
        val isKnown = provider != null && provider.call(emptyList()) { allTracks }.any { it.id == trackId.value() }
        if (isKnown) return

        warnOnce("unknown-minecraft-track:${trackId.asString()}") {
            val suggestions = getAllTracks()
                .map { it.key() }
                .filter { it.value() == trackId.value() && it.namespace() != Key.MINECRAFT_NAMESPACE }
            buildString {
                append("Track '${trackId.asString()}' doesn't exist. ")
                append("Track ID should have the namespace of its plugin, but `Key.key(\"${trackId.value()}\")` adds 'minecraft:'.")
                if (suggestions.isNotEmpty()) append(" Did you mean ${suggestions.joinToString { "'${it.asString()}'" }}?")
            }
        }
    }

    private fun requireValidAmount(amount: Double) {
        require(amount >= 0 && amount.isFinite()) { "'amount' should be non-negative and finite, but it was $amount" }
    }

    /** Returns the given [tracks] of this provider, skipping tracks with invalid or duplicated IDs. */
    private fun LevelProviderEntry.levelTracks(tracks: Collection<LevelProvider.Track>): List<LevelTrack> =
        tracks
            .filter { isValidPath(it.id) }
            .groupBy { it.id }
            .map { (path, tracks) ->
                if (tracks.size > 1) {
                    warnOnce("duplicate-track:${service.id}:$path") {
                        "Level provider '${service.id}' has ${tracks.size} tracks with the same ID '$path', " +
                                "only the first one is used. Please, report it to ${plugin.authorsMention}."
                    }
                }
                levelTrack(tracks.first())
            }

    private fun LevelProviderEntry.isValidPath(path: String): Boolean {
        val isValid = path.isNotEmpty() && Key.parseableValue(path)
        if (!isValid) {
            warnOnce("invalid-path:${service.id}:$path") {
                "Level provider '${service.id}' returned invalid track ID '$path', such tracks are ignored. " +
                        "Track IDs may contain only a-z, 0-9, '.', '_', '-' and '/'. " +
                        "Please, report it to ${plugin.authorsMention}."
            }
        }
        return isValid
    }

    private fun LevelProviderEntry.levelTrack(track: LevelProvider.Track): LevelTrack =
        MimicLevelTrack(Key.key(service.id, track.id), track.name)

    private inline fun warnOnce(key: String, message: () -> String) {
        if (loggedWarnings.add(key)) Log.w(message())
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
internal fun Mimic.getLevelProviderIds(): Set<String> = (levels as MimicLevels).allProviderIds

/** ID of the provider of the primary track. */
internal fun Mimic.getPrimaryLevelProviderId(): String? = (levels as MimicLevels).primaryProvider?.service?.id
