// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.level

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LevelProgressTest {

    @Test
    fun `when build without exp and max level - should use defaults`() {
        val progress = LevelProgress.level(5).minLevel(1).build()

        assertSoftly(progress) {
            level shouldBe 5
            minLevel shouldBe 1
            maxLevel shouldBe -1
            hasMaxLevel() shouldBe false
            hasExp() shouldBe false
            exp shouldBe 0.0
            expToNextLevel shouldBe -1.0
            hasExpToNextLevel() shouldBe false
        }
    }

    @Test
    fun `when build with exp - should mark that track has exp`() {
        val progress = LevelProgress.level(5).minLevel(0).maxLevel(99).exp(10.0).expToNextLevel(100.0).build()

        assertSoftly(progress) {
            maxLevel shouldBe 99
            hasMaxLevel() shouldBe true
            hasExp() shouldBe true
            exp shouldBe 10.0
            expToNextLevel shouldBe 100.0
            hasExpToNextLevel() shouldBe true
        }
    }

    @Test
    fun `when build without min level - should throw`() {
        shouldThrow<IllegalStateException> { LevelProgress.level(5).build() }
    }

    @Test
    fun `when level is lower than min level - should throw`() {
        shouldThrow<IllegalArgumentException> { LevelProgress.level(0).minLevel(1) }
    }

    @Test
    fun `when max level is lower than min level - should throw`() {
        shouldThrow<IllegalStateException> { LevelProgress.level(1).minLevel(1).maxLevel(0).build() }
    }

    @Test
    fun `when level is higher than max level - should build`() {
        LevelProgress.level(100).minLevel(1).maxLevel(99).build().level shouldBe 100
    }

    @Test
    fun `when value is negative - should throw`() {
        assertSoftly {
            shouldThrow<IllegalArgumentException> { LevelProgress.level(-1) }
            shouldThrow<IllegalArgumentException> { LevelProgress.level(1).minLevel(-1) }
            shouldThrow<IllegalArgumentException> { LevelProgress.level(1).maxLevel(-1) }
            shouldThrow<IllegalArgumentException> { LevelProgress.level(1).exp(-1.0) }
            shouldThrow<IllegalArgumentException> { LevelProgress.level(1).expToNextLevel(-1.0) }
        }
    }

    @Test
    fun `when build with exp to next level but without exp - should throw`() {
        shouldThrow<IllegalStateException> { LevelProgress.level(5).minLevel(1).expToNextLevel(100.0).build() }
    }

    @Test
    fun `when compare progress - should compare values`() {
        LevelProgress.level(5).minLevel(1).exp(1.0).build() shouldBe LevelProgress.level(5).minLevel(1).exp(1.0).build()
    }
}
