// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mcmmo

import com.gmail.nossr50.datatypes.player.McMMOPlayer
import com.gmail.nossr50.datatypes.skills.PrimarySkillType
import com.gmail.nossr50.mcMMO
import com.gmail.nossr50.util.player.UserManager
import com.gmail.nossr50.util.skills.SkillTools
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ru.endlesscode.mimic.level.LevelProgress
import ru.endlesscode.mimic.level.LevelProvider

/**
 * mcMMO skills as tracks: `mcmmo:mining`, `mcmmo:swords`.
 * Child skills (`mcmmo:salvage`, `mcmmo:smelting`) and the primary track `mcmmo:power` are read-only.
 */
internal class McMmoLevelProvider(
    private val getMcMmoPlayer: (Player) -> McMMOPlayer? = UserManager::getPlayer,
    private val getSkillTools: () -> SkillTools = { mcMMO.p.skillTools },
    private val getPowerLevelCap: () -> Int = { mcMMO.p.generalConfig.powerLevelCap },
) : LevelProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("mcMMO")

    override val allTracks: Collection<LevelProvider.Track>
        get() {
            val skillTools = getSkillTools()
            val skillTracks = PrimarySkillType.entries.map { skill ->
                LevelProvider.Track(skill.trackId, Component.text(skillTools.getLocalizedSkillName(skill)))
            }
            return skillTracks + POWER_TRACK
        }

    override fun getTracks(player: Player): Collection<LevelProvider.Track> =
        if (getMcMmoPlayer(player) != null) allTracks else emptyList()

    override fun getPrimaryTrack(player: Player): LevelProvider.Track = POWER_TRACK

    override fun getProgress(player: Player, trackId: String): LevelProgress? {
        val mcMmoPlayer = getMcMmoPlayer(player) ?: return null
        if (trackId == POWER_TRACK.id) {
            return LevelProgress.level(mcMmoPlayer.powerLevel)
                .minLevel(0)
                .maxLevelIfCapped(getPowerLevelCap())
                .build()
        }

        val skill = findSkill(trackId) ?: return null
        val level = mcMmoPlayer.getSkillLevel(skill)
        // Child skill levels are calculated from their parent skills, they have no experience
        if (SkillTools.isChildSkill(skill)) return LevelProgress.level(level).minLevel(0).build()

        val levelCap = getSkillTools().getLevelCap(skill)
        val builder = LevelProgress.level(level)
            .minLevel(0)
            .maxLevelIfCapped(levelCap)
            .exp(mcMmoPlayer.getSkillXpLevelRaw(skill).toDouble().coerceAtLeast(0.0))
        if (level < levelCap) builder.expToNextLevel(mcMmoPlayer.getXpToLevel(skill).toDouble())
        return builder.build()
    }

    override fun setLevel(player: Player, trackId: String, level: Int): Boolean {
        val mcMmoPlayer = getMcMmoPlayer(player) ?: return false
        val skill = findParentSkill(trackId) ?: return false
        val currentLevel = mcMmoPlayer.getSkillLevel(skill)
        // Both reset experience within the level.
        // addLevels fires the level-up event, so mcMMO gives its level-up rewards.
        if (level > currentLevel) {
            mcMmoPlayer.addLevels(skill, level - currentLevel)
        } else {
            mcMmoPlayer.modifySkill(skill, level)
        }
        return true
    }

    override fun giveExp(player: Player, trackId: String, amount: Double): Boolean {
        val mcMmoPlayer = getMcMmoPlayer(player) ?: return false
        val skill = findParentSkill(trackId) ?: return false
        // Skips multipliers and party sharing, but keeps level-ups and their rewards
        mcMmoPlayer.addXp(skill, amount.toFloat())
        return true
    }

    override fun takeExp(player: Player, trackId: String, amount: Double): Boolean {
        val mcMmoPlayer = getMcMmoPlayer(player) ?: return false
        val skill = findParentSkill(trackId) ?: return false

        // mcMMO can't lower levels by taking experience, so lower them one by one.
        // The requirement is read after lowering because it depends on the level, or on the power level.
        var level = mcMmoPlayer.getSkillLevel(skill)
        var exp = mcMmoPlayer.getSkillXpLevelRaw(skill) - amount
        while (exp < 0 && level > 0) {
            level--
            mcMmoPlayer.modifySkill(skill, level)
            exp += mcMmoPlayer.getXpToLevel(skill)
        }
        mcMmoPlayer.setSkillXpLevel(skill, exp.coerceAtLeast(0.0).toFloat())
        return true
    }

    private fun findSkill(trackId: String): PrimarySkillType? = PrimarySkillType.entries.find { it.trackId == trackId }

    /** Finds a skill that can be changed. Child skills can't, their levels are calculated from parent skills. */
    private fun findParentSkill(trackId: String): PrimarySkillType? =
        findSkill(trackId)?.takeUnless(SkillTools::isChildSkill)

    private val PrimarySkillType.trackId: String
        get() = name.lowercase()

    /** mcMMO uses [Int.MAX_VALUE] for unlimited levels. */
    private fun LevelProgress.Builder.maxLevelIfCapped(cap: Int): LevelProgress.Builder =
        if (cap == Int.MAX_VALUE) this else maxLevel(cap)

    companion object {
        const val ID = "mcmmo"

        private val POWER_TRACK = LevelProvider.Track("power", Component.text("Power Level"))
    }
}
