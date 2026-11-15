// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.fabled

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.level.LevelProgress
import ru.endlesscode.mimic.level.LevelProvider
import studio.magemonkey.fabled.api.enums.ExpSource
import studio.magemonkey.fabled.api.player.PlayerClass
import studio.magemonkey.fabled.api.player.PlayerData

/**
 * Fabled class groups as tracks: `fabled:class`, `fabled:race`. The level belongs to the group, not to the class:
 * it is kept when the player professes into a subclass. The primary track is the group of the main class.
 */
internal class FabledLevelProvider(
    private val fabled: FabledWrapper = FabledWrapper(),
) : LevelProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = fabled.isEnabled

    override val allTracks: Collection<LevelProvider.Track>
        get() = fabled.groups.map(::track)

    override fun getTracks(player: Player): Collection<LevelProvider.Track> {
        val data = player.loadedData() ?: return emptyList()
        return fabled.groups.filter { data.getClass(it) != null }.map(::track)
    }

    override fun getPrimaryTrack(player: Player): LevelProvider.Track? =
        player.loadedData()?.mainClass?.let { track(it.data.group) }

    override fun getProgress(player: Player, trackId: String): LevelProgress? {
        val playerClass = player.findClass(trackId) ?: return null
        val maxLevel = playerClass.data.maxLevel.coerceAtLeast(MIN_LEVEL)
        val level = playerClass.level.coerceIn(MIN_LEVEL, maxLevel)
        val builder = LevelProgress.level(level)
            .minLevel(MIN_LEVEL)
            .maxLevel(maxLevel)
            .exp(playerClass.exp.coerceAtLeast(0.0))
        if (level < maxLevel) builder.expToNextLevel(playerClass.requiredExp.toDouble().coerceAtLeast(0.0))
        return builder.build()
    }

    override fun setLevel(player: Player, trackId: String, level: Int): Boolean {
        val playerClass = player.findClass(trackId) ?: return false

        // The same as Fabled's level command: gives or takes back points of the levels and fires events
        val levels = level - playerClass.level
        if (levels > 0) playerClass.giveLevels(levels) else if (levels < 0) playerClass.loseLevels(-levels)
        playerClass.exp = 0.0
        return true
    }

    override fun giveExp(player: Player, trackId: String, amount: Double): Boolean {
        val playerClass = player.findClass(trackId) ?: return false
        // The same as Fabled's exp command: it is the only source not scaled by the experience attribute.
        // Fabled rounds the amount up and ignores sources disabled in the class config.
        if (!playerClass.data.receivesExp(ExpSource.COMMAND)) return false

        playerClass.giveExp(amount, ExpSource.COMMAND)
        return true
    }

    override fun takeExp(player: Player, trackId: String, amount: Double): Boolean {
        val playerClass = player.findClass(trackId) ?: return false

        // Fabled lowers levels itself, but doesn't accept zero
        if (amount > 0) playerClass.loseExp(amount, false, true, true)
        return true
    }

    private fun Player.loadedData(): PlayerData? = fabled.getLoadedData(this)

    /** The class the player has in the group of the track. */
    private fun Player.findClass(trackId: String): PlayerClass? {
        val data = loadedData() ?: return null
        val group = fabled.groups.find { groupId(it) == trackId } ?: return null
        return data.getClass(group)
    }

    companion object {
        const val ID = "fabled"
    }
}

private const val MIN_LEVEL = 1

// Groups don't have display names
private fun track(group: String): LevelProvider.Track = LevelProvider.Track(groupId(group), Component.text(group))
