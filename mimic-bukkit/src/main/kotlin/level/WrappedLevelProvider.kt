// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.level

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.WrappedMimicService

internal class WrappedLevelProvider(
    private val delegate: LevelProvider,
    plugin: Plugin,
) : WrappedMimicService(delegate, plugin.name), LevelProvider {

    override val allTracks: Collection<LevelProvider.Track>
        get() = delegate.allTracks

    override fun getTracks(player: Player): Collection<LevelProvider.Track> = delegate.getTracks(player)
    override fun getPrimaryTrack(player: Player): LevelProvider.Track? = delegate.getPrimaryTrack(player)
    override fun getProgress(player: Player, trackId: String): LevelProgress? = delegate.getProgress(player, trackId)

    override fun setLevel(player: Player, trackId: String, level: Int): Boolean =
        delegate.setLevel(player, trackId, level)

    override fun giveExp(player: Player, trackId: String, amount: Double): Boolean =
        delegate.giveExp(player, trackId, amount)

    override fun takeExp(player: Player, trackId: String, amount: Double): Boolean =
        delegate.takeExp(player, trackId, amount)
}
