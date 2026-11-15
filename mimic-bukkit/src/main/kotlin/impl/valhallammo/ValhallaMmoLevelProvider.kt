// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.valhallammo

import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent.ExperienceGainReason
import me.athlaeos.valhallammo.skills.skills.Skill
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import ru.endlesscode.mimic.level.LevelProgress
import ru.endlesscode.mimic.level.LevelProvider

/**
 * ValhallaMMO skills as tracks: `valhallammo:mining`, `valhallammo:light_armor`.
 * The primary track is `valhallammo:power`. Unlike power levels of other plugins, it is a skill with its own
 * experience given for levels of other skills, so it can be changed like any other skill.
 */
internal class ValhallaMmoLevelProvider(
    private val valhallaMmo: ValhallaMmoWrapper = ValhallaMmoWrapper(),
) : LevelProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = valhallaMmo.isEnabled

    override val allTracks: Collection<LevelProvider.Track>
        get() = skills().map { (trackId, skill) -> skill.toTrack(trackId) }

    override fun getTracks(player: Player): Collection<LevelProvider.Track> =
        if (valhallaMmo.isLoaded(player)) allTracks else emptyList()

    override fun getPrimaryTrack(player: Player): LevelProvider.Track? =
        findSkill(POWER_TRACK_ID)?.toTrack(POWER_TRACK_ID)

    override fun getProgress(player: Player, trackId: String): LevelProgress? {
        val skill = findSkill(trackId) ?: return null
        if (!valhallaMmo.isLoaded(player)) return null

        val profile = valhallaMmo.getProfile(player, skill)
        val level = profile.level
        // Perks may limit the level for the player
        val maxLevel = minOf(skill.maxLevel, valhallaMmo.getMaxAllowedLevel(player, skill))
        val builder = LevelProgress.level(level)
            .minLevel(MIN_LEVEL)
            .maxLevel(maxLevel)
            .exp(profile.exp.coerceAtLeast(0.0))
        if (level < maxLevel) builder.expToNextLevel(skill.expForLevel(level + 1).coerceAtLeast(0.0))
        return builder.build()
    }

    override fun setLevel(player: Player, trackId: String, level: Int): Boolean {
        val skill = findChangeableSkill(player, trackId) ?: return false
        val profile = valhallaMmo.getProfile(player, skill)
        val currentLevel = profile.level

        // ValhallaMMO changes levels only through experience, so it gives level-up rewards and reverts them.
        // When lowering levels, it returns the requirement of the new level, not of the next one.
        if (level != currentLevel) {
            val expToLevel = (currentLevel + 1..level).sumOf(skill::expForLevel) -
                    (level until currentLevel).sumOf(skill::expForLevel)
            // Aim at the middle of the level to not miss it because of rounding
            val expWithinLevel = skill.expForLevel(level) / 2
            skill.addEXP(player, expToLevel - profile.exp + expWithinLevel, false, ExperienceGainReason.PLUGIN)
        }

        // The level stays the same if the change was cancelled, keep experience in this case
        val changedProfile = valhallaMmo.getProfile(player, skill)
        if (changedProfile.level == level && changedProfile.exp != 0.0) {
            skill.addEXP(player, -changedProfile.exp, true, ExperienceGainReason.PLUGIN)
        }
        return true
    }

    override fun giveExp(player: Player, trackId: String, amount: Double): Boolean {
        val skill = findChangeableSkill(player, trackId) ?: return false
        // Experience given by a plugin skips multipliers and party sharing, but keeps level-up rewards
        skill.addEXP(player, amount, false, ExperienceGainReason.PLUGIN)
        return true
    }

    override fun takeExp(player: Player, trackId: String, amount: Double): Boolean {
        val skill = findChangeableSkill(player, trackId) ?: return false
        // ValhallaMMO lowers levels and reverts their rewards itself, the level and experience can't go below zero
        skill.addEXP(player, -amount, false, ExperienceGainReason.PLUGIN)
        return true
    }

    private fun findSkill(trackId: String): Skill? = skills()[trackId]

    /** Finds a skill the [player] can gain and lose experience in, ValhallaMMO silently ignores changes of others. */
    private fun findChangeableSkill(player: Player, trackId: String): Skill? {
        val skill = findSkill(trackId) ?: return null
        val isChangeable = valhallaMmo.isLoaded(player) &&
                skill.hasPermissionAccess(player) &&
                !skill.hasReachedLimit(player) &&
                valhallaMmo.getProfile(player, skill).level < valhallaMmo.getMaxAllowedLevel(player, skill)
        return skill.takeIf { isChangeable }
    }

    private fun Skill.toTrack(trackId: String): LevelProvider.Track =
        LevelProvider.Track(trackId, LEGACY_SERIALIZER.deserialize(displayName))

    /** Skills by track ID. */
    private fun skills(): Map<String, Skill> =
        valhallaMmo.skills
            .filter { it.isLevelableSkill }
            .associateBy { it.type.lowercase() }

    companion object {
        const val ID = "valhallammo"

        private const val POWER_TRACK_ID = "power"
        private const val MIN_LEVEL = 0

        // Skill names are already colored by ValhallaMMO
        private val LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build()
    }
}
