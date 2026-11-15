// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.jobs

import com.gamingmesh.jobs.Jobs
import com.gamingmesh.jobs.container.Job
import com.gamingmesh.jobs.container.JobsPlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player

internal class JobsWrapper {

    val isEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("Jobs")

    fun getJobs(): List<Job> = Jobs.getJobs()

    /** Returns `null` until Jobs loads the player's data. */
    fun getJobsPlayer(player: Player): JobsPlayer? = Jobs.getPlayerManager().getJobsPlayer(player)

    /** Raises the level and runs level-up commands for skipped levels. Fires no events and sends no messages. */
    fun promoteJob(jobsPlayer: JobsPlayer, job: Job, levels: Int) =
        Jobs.getPlayerManager().promoteJob(jobsPlayer, job, levels, true)

    /** Lowers the level. Fires no events and sends no messages. */
    fun demoteJob(jobsPlayer: JobsPlayer, job: Job, levels: Int) =
        Jobs.getPlayerManager().demoteJob(jobsPlayer, job, levels)

    /** Skips boosts and payment limits, but keeps level-ups with their messages, commands and event. */
    fun addExperience(jobsPlayer: JobsPlayer, job: Job, experience: Double) =
        Jobs.getPlayerManager().addExperience(jobsPlayer, job, experience)
}
