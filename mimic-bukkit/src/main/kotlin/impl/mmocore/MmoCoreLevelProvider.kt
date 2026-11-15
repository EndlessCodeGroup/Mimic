// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mmocore

import net.Indyuce.mmocore.api.event.PlayerLevelChangeEvent.Reason
import net.Indyuce.mmocore.api.player.PlayerData
import net.Indyuce.mmocore.experience.EXPSource
import net.Indyuce.mmocore.experience.Profession
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import ru.endlesscode.mimic.level.LevelProgress
import ru.endlesscode.mimic.level.LevelProvider

/**
 * MMOCore levels: the primary track `mmocore:level` is the level of the current class,
 * and professions are tracks `mmocore:profession/mining`.
 */
internal class MmoCoreLevelProvider(
    private val mmoCore: MmoCoreWrapper = MmoCoreWrapper(),
) : LevelProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = mmoCore.isEnabled

    override val allTracks: Collection<LevelProvider.Track>
        get() = listOf(LEVEL_TRACK) + mmoCore.professions.map { profession ->
            LevelProvider.Track(profession.trackId, LEGACY_SERIALIZER.deserialize(profession.name))
        }

    override fun getTracks(player: Player): Collection<LevelProvider.Track> =
        if (player.loadedData() != null) allTracks else emptyList()

    override fun getPrimaryTrack(player: Player): LevelProvider.Track = LEVEL_TRACK

    override fun getProgress(player: Player, trackId: String): LevelProgress? {
        val data = player.loadedData() ?: return null
        if (trackId == LEVEL_TRACK.id) {
            return buildProgress(
                level = data.level,
                maxLevel = data.profess.maxLevel,
                exp = data.experience,
                expToNextLevel = { data.levelUpExperience },
            )
        }

        val profession = findProfession(trackId) ?: return null
        val professions = data.collectionSkills
        return buildProgress(
            level = professions.getLevel(profession),
            maxLevel = profession.maxLevel,
            exp = professions.getExperience(profession),
            expToNextLevel = { professions.getLevelUpExperience(profession) },
        )
    }

    override fun setLevel(player: Player, trackId: String, level: Int): Boolean {
        val data = player.loadedData() ?: return false
        // Give the experience table rewards like MMOCore does on level-up
        if (trackId == LEVEL_TRACK.id) {
            val currentLevel = data.level
            for (newLevel in currentLevel + 1..level) data.profess.updateAdvancement(data, newLevel)
            data.setLevel(level, levelChangeReason(currentLevel, level))
            data.experience = 0.0
            return true
        }

        val profession = findProfession(trackId) ?: return false
        val professions = data.collectionSkills
        val currentLevel = professions.getLevel(profession)
        for (newLevel in currentLevel + 1..level) profession.updateAdvancement(data, newLevel)
        professions.setLevel(profession, level, levelChangeReason(currentLevel, level))
        professions.setExperience(profession, 0.0)
        return true
    }

    override fun giveExp(player: Player, trackId: String, amount: Double): Boolean {
        val data = player.loadedData() ?: return false
        // Skips party sharing. MMOCore always applies its experience boosters and stats.
        if (trackId == LEVEL_TRACK.id) {
            data.giveExperience(amount, EXPSource.OTHER, null, false)
            return true
        }

        val profession = findProfession(trackId) ?: return false
        data.collectionSkills.giveExperience(profession, amount, EXPSource.OTHER, null, false)
        return true
    }

    override fun takeExp(player: Player, trackId: String, amount: Double): Boolean {
        val data = player.loadedData() ?: return false
        // MMOCore can't lower levels by taking experience, so lower them one by one.
        // The requirement is read after lowering, because formula curves depend on the current main level.
        if (trackId == LEVEL_TRACK.id) {
            data.experience = lowerLevels(
                level = data.level,
                exp = data.experience - amount,
                setLevel = { data.setLevel(it, Reason.COMMAND) },
                expToNextLevel = { data.levelUpExperience },
            )
            return true
        }

        val profession = findProfession(trackId) ?: return false
        val professions = data.collectionSkills
        val exp = lowerLevels(
            level = professions.getLevel(profession),
            exp = professions.getExperience(profession) - amount,
            setLevel = { professions.setLevel(profession, it, Reason.COMMAND) },
            expToNextLevel = { professions.getLevelUpExperience(profession) },
        )
        professions.setExperience(profession, exp)
        return true
    }

    private fun Player.loadedData(): PlayerData? = mmoCore.getLoadedData(this)

    private fun findProfession(trackId: String): Profession? = mmoCore.professions.find { it.trackId == trackId }

    /**
     * Lowers the level from [level] until [exp] is non-negative or the level is minimal.
     * Returns the experience within the new level.
     */
    private inline fun lowerLevels(
        level: Int,
        exp: Double,
        setLevel: (Int) -> Unit,
        expToNextLevel: () -> Long,
    ): Double {
        var currentLevel = level
        var currentExp = exp
        while (currentExp < 0 && currentLevel > MIN_LEVEL) {
            currentLevel--
            setLevel(currentLevel)
            currentExp += expToNextLevel()
        }
        return currentExp.coerceAtLeast(0.0)
    }

    /** Builds progress, [maxLevel] is `0` if the level is unlimited. */
    private inline fun buildProgress(
        level: Int,
        maxLevel: Int,
        exp: Double,
        expToNextLevel: () -> Long,
    ): LevelProgress {
        val builder = LevelProgress.level(level)
            .minLevel(MIN_LEVEL)
            .exp(exp.coerceAtLeast(0.0))
        if (maxLevel > 0) builder.maxLevel(maxLevel)
        if (maxLevel !in 1..level) builder.expToNextLevel(expToNextLevel().toDouble().coerceAtLeast(0.0))
        return builder.build()
    }

    /** `LEVEL_UP` runs MMOCore triggers for every new level, like level-up commands. */
    private fun levelChangeReason(currentLevel: Int, newLevel: Int): Reason =
        if (newLevel > currentLevel) Reason.LEVEL_UP else Reason.COMMAND

    companion object {
        const val ID = "mmocore"
        const val LEVEL_TRACK_ID = "level"

        private const val MIN_LEVEL = 1
        private val LEVEL_TRACK = LevelProvider.Track(LEVEL_TRACK_ID, Component.text("Level"))
        // Profession names are raw config strings with `&` color codes
        private val LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand()
    }
}

private val Profession.trackId: String
    get() = "profession/$id"
