// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.auraskills

import dev.aurelium.auraskills.api.AuraSkillsApi
import dev.aurelium.auraskills.api.registry.NamespacedId
import dev.aurelium.auraskills.api.skill.Skill
import dev.aurelium.auraskills.api.user.SkillsUser
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ru.endlesscode.mimic.level.LevelProgress
import ru.endlesscode.mimic.level.LevelProvider

/**
 * AuraSkills skills as tracks: `auraskills:mining`, or `auraskills:myaddon/fishing` for skills of other namespaces.
 * The primary track is the read-only `auraskills:power`, the sum of all skill levels.
 */
internal class AuraSkillsLevelProvider(
    private val getApi: () -> AuraSkillsApi = AuraSkillsApi::get,
) : LevelProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("AuraSkills")

    private val api: AuraSkillsApi get() = getApi()

    override val allTracks: Collection<LevelProvider.Track>
        get() {
            val locale = api.messageManager.defaultLanguage
            val skillTracks = enabledSkills().map { (trackId, skill) ->
                LevelProvider.Track(trackId, Component.text(skill.getDisplayName(locale, false)))
            }
            return skillTracks + POWER_TRACK
        }

    override fun getTracks(player: Player): Collection<LevelProvider.Track> =
        if (player.loadedUser() != null) allTracks else emptyList()

    override fun getPrimaryTrack(player: Player): LevelProvider.Track = POWER_TRACK

    override fun getProgress(player: Player, trackId: String): LevelProgress? {
        val user = player.loadedUser() ?: return null
        if (trackId == POWER_TRACK.id) return getPowerProgress(user)

        val skill = findSkill(trackId) ?: return null
        val startLevel = api.mainConfig.startLevel
        // A level may be below the start level after the start level is raised.
        // AuraSkills raises such levels on load, so show the level it will have.
        val level = user.getSkillLevel(skill).coerceAtLeast(startLevel)
        val builder = LevelProgress.level(level)
            .minLevel(startLevel)
            .maxLevel(skill.maxLevel)
            .exp(user.getSkillXp(skill).coerceAtLeast(0.0))

        if (level < skill.maxLevel) {
            // AuraSkills returns 0 when the level is beyond the configured requirements, so it can't be reached
            val expToNextLevel = api.xpRequirements.getXpRequired(skill, level + 1)
            if (expToNextLevel > 0) builder.expToNextLevel(expToNextLevel.toDouble())
        }
        return builder.build()
    }

    override fun setLevel(player: Player, trackId: String, level: Int): Boolean {
        val user = player.loadedUser() ?: return false
        val skill = findSkill(trackId) ?: return false
        // Runs AuraSkills level-up commands for skipped levels and revert commands for lost levels
        user.setSkillLevel(skill, level)
        user.setSkillXp(skill, 0.0)
        return true
    }

    override fun giveExp(player: Player, trackId: String, amount: Double): Boolean {
        val user = player.loadedUser() ?: return false
        val skill = findSkill(trackId) ?: return false
        // Skips multipliers but keeps level-up rewards. Several level-ups are applied one by one with a delay.
        user.addSkillXpRaw(skill, amount)
        return true
    }

    override fun takeExp(player: Player, trackId: String, amount: Double): Boolean {
        val user = player.loadedUser() ?: return false
        val skill = findSkill(trackId) ?: return false
        val startLevel = api.mainConfig.startLevel
        val currentLevel = user.getSkillLevel(skill)

        // AuraSkills can't take experience, so lower levels until the remaining experience is non-negative
        var level = currentLevel
        var exp = user.getSkillXp(skill) - amount
        while (exp < 0 && level > startLevel) {
            // AuraSkills returns 0 for levels beyond the configured requirements, so there is no experience to take
            val levelExp = api.xpRequirements.getXpRequired(skill, level)
            if (levelExp <= 0) break
            exp += levelExp
            level--
        }
        if (level != currentLevel) user.setSkillLevel(skill, level)
        user.setSkillXp(skill, exp.coerceAtLeast(0.0))
        return true
    }

    private fun getPowerProgress(user: SkillsUser): LevelProgress {
        val skills = enabledSkills().values
        val minLevel = api.mainConfig.startLevel * skills.size
        // Levels below the start level are raised on load, the same as in getProgress
        return LevelProgress.level(user.powerLevel.coerceAtLeast(minLevel))
            .minLevel(minLevel)
            .maxLevel(skills.sumOf { it.maxLevel })
            .build()
    }

    private fun Player.loadedUser(): SkillsUser? = api.getUser(uniqueId).takeIf { it.isLoaded }

    private fun findSkill(trackId: String): Skill? = enabledSkills()[trackId]

    /** Enabled skills by track ID. */
    private fun enabledSkills(): Map<String, Skill> =
        api.globalRegistry.skills
            .filter { it.isEnabled }
            .associateBy { it.trackId }

    private val Skill.trackId: String
        get() = if (id.namespace == NamespacedId.AURASKILLS) id.key else "${id.namespace}/${id.key}"

    companion object {
        const val ID = "auraskills"

        private val POWER_TRACK = LevelProvider.Track("power", Component.text("Power Level"))
    }
}
