// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.jobs

import com.gamingmesh.jobs.container.Job
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.internal.NormalizedIds
import ru.endlesscode.mimic.level.LevelProgress
import ru.endlesscode.mimic.level.LevelProvider

/**
 * Jobs Reborn jobs as tracks: `jobs:miner`, `jobs:woodcutter`. A player has tracks of the jobs they have joined.
 * The primary track is the read-only `jobs:total`, the sum of levels of the joined jobs.
 */
internal class JobsLevelProvider(
    private val jobs: JobsWrapper = JobsWrapper(),
) : LevelProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = jobs.isEnabled

    override val allTracks: Collection<LevelProvider.Track>
        get() = jobs.getJobs().map { it.toTrack() } + TOTAL_TRACK

    override fun getTracks(player: Player): Collection<LevelProvider.Track> {
        val jobsPlayer = jobs.getJobsPlayer(player) ?: return emptyList()
        val joinedJobs = jobs.getJobs().filter { jobsPlayer.getJobProgression(it) != null }
        return joinedJobs.map { it.toTrack() } + TOTAL_TRACK
    }

    override fun getPrimaryTrack(player: Player): LevelProvider.Track = TOTAL_TRACK

    override fun getProgress(player: Player, trackId: String): LevelProgress? {
        val jobsPlayer = jobs.getJobsPlayer(player) ?: return null
        if (trackId == TOTAL_TRACK.id) return LevelProgress.level(jobsPlayer.totalLevels).minLevel(0).build()

        val job = findJob(trackId) ?: return null
        val progression = jobsPlayer.getJobProgression(job) ?: return null
        // Jobs commands can set any level, and a custom equation can give a negative requirement
        val level = progression.level.coerceAtLeast(MIN_LEVEL)
        val builder = LevelProgress.level(level)
            .minLevel(MIN_LEVEL)
            .exp(progression.experience.coerceAtLeast(0.0))

        // Jobs uses 0 for unlimited levels. Permissions can raise the max level only if the job has one.
        val maxLevel = if (job.maxLevel > 0) job.getMaxLevel(jobsPlayer) else 0
        if (maxLevel > 0) builder.maxLevel(maxLevel)
        if (maxLevel !in 1..level) {
            builder.expToNextLevel(progression.maxExperience.toDouble().coerceAtLeast(0.0))
        }
        return builder.build()
    }

    override fun setLevel(player: Player, trackId: String, level: Int): Boolean {
        val jobsPlayer = jobs.getJobsPlayer(player) ?: return false
        val job = findJob(trackId) ?: return false
        val progression = jobsPlayer.getJobProgression(job) ?: return false

        // Reset experience first, Jobs checks it for level-ups when the level changes
        progression.setExperience(0.0)
        val levels = level - progression.level
        if (levels > 0) {
            jobs.promoteJob(jobsPlayer, job, levels)
        } else if (levels < 0) {
            jobs.demoteJob(jobsPlayer, job, -levels)
        }
        return true
    }

    override fun giveExp(player: Player, trackId: String, amount: Double): Boolean {
        val jobsPlayer = jobs.getJobsPlayer(player) ?: return false
        val job = findJob(trackId) ?: return false
        jobsPlayer.getJobProgression(job) ?: return false
        jobs.addExperience(jobsPlayer, job, amount)
        return true
    }

    override fun takeExp(player: Player, trackId: String, amount: Double): Boolean {
        val jobsPlayer = jobs.getJobsPlayer(player) ?: return false
        val job = findJob(trackId) ?: return false
        val progression = jobsPlayer.getJobProgression(job) ?: return false

        // Jobs lowers levels only with 'AllowDelevel' enabled, so lower them here to work the same with any config
        var level = progression.level
        var exp = progression.experience - amount
        while (exp < 0 && level > MIN_LEVEL) {
            level--
            exp += progression.getMaxExperience(level)
        }
        val lostLevels = progression.level - level
        progression.setExperience(exp.coerceAtLeast(0.0))
        if (lostLevels > 0) jobs.demoteJob(jobsPlayer, job, lostLevels)
        return true
    }

    private fun findJob(trackId: String): Job? = jobs.getJobs().find { it.trackId == trackId }

    companion object {
        const val ID = "jobs"

        private const val MIN_LEVEL = 1
        private val TOTAL_TRACK = LevelProvider.Track("total", Component.text("Total Level"))
    }
}

/** Job name converted to an ID: `Wood Cutter` becomes `wood_cutter`. */
private val Job.trackId: String
    get() = NormalizedIds.normalize(name)

private fun Job.toTrack() = LevelProvider.Track(trackId, Component.text(jobFullName))
