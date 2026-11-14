// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // LevelSystem API is deprecated but still supported

package ru.endlesscode.mimic.level

import org.bukkit.entity.Player

/**
 * Exposes the given [LevelSystem][BukkitLevelSystem] [provider] as a provider of the single primary [track][SingleTrackLevelProvider.DEFAULT_TRACK].
 *
 * [LevelSystem] throws [IllegalStateException] if the player's data doesn't exist,
 * and [UnsupportedOperationException] if an operation isn't supported.
 * Reads return `null` and writes return `false` in these cases.
 */
internal class LevelSystemTrackProvider(
    private val provider: BukkitLevelSystem.Provider,
) : SingleTrackLevelProvider() {

    override val id: String get() = provider.id
    override val isEnabled: Boolean get() = provider.isEnabled

    override fun getProgress(player: Player): LevelProgress? = try {
        val system = provider.getSystem(player)
        val expToNextLevel = system.totalExpToNextLevel
        val progress = LevelProgress.level(system.level)
            .minLevel(0)
            // Converters off by one may give negative exp within the level
            .exp(system.exp.coerceAtLeast(0.0))
        // -1 means that the level-up is impossible
        if (expToNextLevel >= 0) progress.expToNextLevel(expToNextLevel)
        progress.build()
    } catch (_: IllegalStateException) {
        null
    }

    override fun setLevel(player: Player, level: Int): Boolean = write(player) { system ->
        system.level = level
        @Suppress("DEPRECATION") // There is no other way to reset exp within the level
        system.exp = 0.0
    }

    override fun giveExp(player: Player, amount: Double): Boolean = write(player) { it.giveExp(amount) }
    override fun takeExp(player: Player, amount: Double): Boolean = write(player) { it.takeExp(amount) }

    private inline fun write(player: Player, action: (BukkitLevelSystem) -> Unit): Boolean = try {
        action(provider.getSystem(player))
        true
    } catch (_: IllegalStateException) {
        false
    } catch (_: UnsupportedOperationException) {
        false
    }
}
