// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.jobs

import com.gamingmesh.jobs.container.Job
import com.gamingmesh.jobs.container.JobProgression
import com.gamingmesh.jobs.container.JobsPlayer
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import ru.endlesscode.mimic.BukkitTestBase
import ru.endlesscode.mimic.level.LevelProgress
import kotlin.test.Test

internal class JobsLevelProviderTest : BukkitTestBase() {

    private val miner = mockJob("Miner")
    private val woodcutter = mockJob("Woodcutter")
    private var isLoaded = true

    /** Level and experience of the joined job stored like in Jobs, requirement is `100 * level`. */
    private var level = 1
    private var exp = 0.0

    private val minerProgression: JobProgression = mockk {
        every { level } answers { this@JobsLevelProviderTest.level }
        every { experience } answers { exp }
        every { maxExperience } answers { 100 * this@JobsLevelProviderTest.level }
        every { getMaxExperience(any()) } answers { 100 * firstArg<Int>() }
        every { setExperience(any()) } answers {
            exp = firstArg()
            false
        }
    }

    /** The player has joined only the miner job. */
    private val jobsPlayer: JobsPlayer = mockk {
        every { getJobProgression(any<Job>()) } returns null
        every { getJobProgression(miner) } returns minerProgression
        every { totalLevels } answers { level }
    }

    private val jobs: JobsWrapper = mockk(relaxUnitFun = true) {
        every { getJobs() } returns listOf(miner, woodcutter)
        every { getJobsPlayer(player) } answers { if (isLoaded) jobsPlayer else null }
        every { demoteJob(any(), any(), any()) } answers { level -= thirdArg<Int>() }
    }

    // SUT
    private val provider = JobsLevelProvider(jobs)

    @Test
    fun `when get all tracks - should return all jobs and total`() {
        provider.allTracks.map { it.id }.shouldContainExactly("miner", "woodcutter", "total")
    }

    @Test
    fun `when job name has invalid characters - should replace them in track id`() {
        every { jobs.getJobs() } returns listOf(mockJob("Wood Cutter"))

        provider.allTracks.map { it.id }.shouldContainExactly("wood_cutter", "total")
    }

    @Test
    fun `when get tracks - should return joined jobs and total`() {
        provider.getTracks(player).map { it.id }.shouldContainExactly("miner", "total")
    }

    @Test
    fun `when get tracks of not loaded player - should return empty list`() {
        isLoaded = false

        provider.getTracks(player).shouldBeEmpty()
    }

    @Test
    fun `when get primary track - should return total`() {
        provider.getPrimaryTrack(player).id shouldBe "total"
    }

    @Test
    fun `when get job progress - should return level and exp`() {
        level = 2
        exp = 150.5

        provider.getProgress(player, "miner") shouldBe
            LevelProgress.level(2).minLevel(1).exp(150.5).expToNextLevel(200.0).build()
    }

    @Test
    fun `when get progress with max level - should use max level of the player`() {
        every { miner.maxLevel } returns 100
        every { miner.getMaxLevel(jobsPlayer) } returns 150
        level = 2

        provider.getProgress(player, "miner") shouldBe
            LevelProgress.level(2).minLevel(1).maxLevel(150).exp(0.0).expToNextLevel(200.0).build()
    }

    @Test
    fun `when get progress at max level - should not have exp to next level`() {
        every { miner.maxLevel } returns 100
        every { miner.getMaxLevel(jobsPlayer) } returns 100
        level = 100

        provider.getProgress(player, "miner") shouldBe
            LevelProgress.level(100).minLevel(1).maxLevel(100).exp(0.0).build()
    }

    @Test
    fun `when get total progress - should return total level`() {
        level = 7

        provider.getProgress(player, "total") shouldBe LevelProgress.level(7).minLevel(0).build()
    }

    @Test
    fun `when get progress of not joined job, unknown track or not loaded player - should return null`() {
        provider.getProgress(player, "woodcutter").shouldBeNull()
        provider.getProgress(player, "unknown").shouldBeNull()
        isLoaded = false
        provider.getProgress(player, "miner").shouldBeNull()
    }

    @Test
    fun `when raise level - should reset exp and promote with commands`() {
        level = 2
        exp = 50.0

        provider.setLevel(player, "miner", 5).shouldBeTrue()

        verifyOrder {
            minerProgression.setExperience(0.0)
            jobs.promoteJob(jobsPlayer, miner, 3)
        }
    }

    @Test
    fun `when lower level - should reset exp and demote`() {
        level = 5
        exp = 50.0

        provider.setLevel(player, "miner", 2).shouldBeTrue()

        level shouldBe 2
        exp shouldBe 0.0
    }

    @Test
    fun `when give exp - should add exact exp`() {
        provider.giveExp(player, "miner", 42.5).shouldBeTrue()

        verify { jobs.addExperience(jobsPlayer, miner, 42.5) }
    }

    @Test
    fun `when take exp within level - should keep level`() {
        level = 2
        exp = 150.0

        provider.takeExp(player, "miner", 50.0).shouldBeTrue()

        level shouldBe 2
        exp shouldBe 100.0
    }

    @Test
    fun `when take exp above level exp - should lower level`() {
        level = 3
        exp = 50.0

        // 50 exp on level 3, then 200 exp of the step from level 2 to 3
        provider.takeExp(player, "miner", 100.0).shouldBeTrue()

        level shouldBe 2
        exp shouldBe 150.0
    }

    @Test
    fun `when take more exp than total - should stop at level 1`() {
        level = 3
        exp = 50.0

        provider.takeExp(player, "miner", 1000.0).shouldBeTrue()

        level shouldBe 1
        exp shouldBe 0.0
    }

    @Test
    fun `when change total, not joined job or unknown track - should not be supported`() {
        for (trackId in listOf("total", "woodcutter", "unknown")) {
            provider.setLevel(player, trackId, 10).shouldBeFalse()
            provider.giveExp(player, trackId, 10.0).shouldBeFalse()
            provider.takeExp(player, trackId, 10.0).shouldBeFalse()
        }
    }

    @Test
    fun `when change level of not loaded player - should return false`() {
        isLoaded = false

        provider.setLevel(player, "miner", 10).shouldBeFalse()
        provider.giveExp(player, "miner", 10.0).shouldBeFalse()
        provider.takeExp(player, "miner", 10.0).shouldBeFalse()
    }

    private fun mockJob(name: String): Job = mockk {
        every { this@mockk.name } returns name
        every { jobFullName } returns name
        every { maxLevel } returns 0
    }
}
