// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.level

import org.jetbrains.annotations.ApiStatus

/**
 * Snapshot of a player's progress on a level track.
 * The maximum level and experience requirements are the ones that apply to this player now,
 * so they may differ between players on the same track.
 *
 * Values are read together and may be stale right after a write, so don't cache them.
 * Progress objects are equal if all their values are equal.
 *
 * Create it with the builder:
 * ```
 * LevelProgress.level(5).minLevel(1).build();
 * LevelProgress.level(5).minLevel(0).maxLevel(99).exp(10.0).expToNextLevel(100.0).build();
 * ```
 * @since 0.10.0
 */
@ApiStatus.NonExtendable
public interface LevelProgress {

    /** Current level. Never lower than [minLevel]. */
    public val level: Int

    /** The lowest level on the track, `0` or `1` depending on the plugin. It is the same for all players. */
    public val minLevel: Int

    /**
     * The highest level the player can reach on the track, `-1` if the level is unlimited. See [hasMaxLevel].
     * It may depend on the player, for example on their permissions or class.
     */
    public val maxLevel: Int

    /** Experience within the current level, `0.0` if the track has no experience. See [hasExp]. */
    public val exp: Double

    /**
     * Total experience required for the step from the current level to the next one, not the remaining part.
     * `0.0` if the next level is free, `-1` if the requirement is unknown or the level is maximal.
     * See [hasExpToNextLevel].
     */
    public val expToNextLevel: Double

    /** Returns `false` if the level is unlimited. */
    public fun hasMaxLevel(): Boolean = maxLevel != -1

    /** Returns `false` if the track has levels only, without experience. */
    public fun hasExp(): Boolean

    /** Returns `false` if the experience required for the next level is unknown, or the level is maximal. */
    public fun hasExpToNextLevel(): Boolean = expToNextLevel != -1.0

    /** Builder of [LevelProgress], use [LevelProgress.level] to create it. */
    public class Builder internal constructor(private val level: Int) {
        private var minLevel = -1
        private var maxLevel = -1
        private var hasExp = false
        private var exp = 0.0
        private var expToNextLevel = -1.0

        init {
            require(level >= 0) { "'level' should be non-negative, but it was $level" }
        }

        /**
         * Sets the lowest level on the track, `0` or `1` depending on the plugin. Required.
         * @throws IllegalArgumentException if [minLevel] is negative or higher than the level.
         */
        public fun minLevel(minLevel: Int): Builder = apply {
            require(minLevel >= 0) { "'minLevel' should be non-negative, but it was $minLevel" }
            require(minLevel <= level) { "'minLevel' ($minLevel) should not be higher than 'level' ($level)" }
            this.minLevel = minLevel
        }

        /**
         * Sets the highest level on the track. Unlimited by default.
         * @throws IllegalArgumentException if [maxLevel] is negative.
         */
        public fun maxLevel(maxLevel: Int): Builder = apply {
            require(maxLevel >= 0) { "'maxLevel' should be non-negative, but it was $maxLevel" }
            this.maxLevel = maxLevel
        }

        /**
         * Sets experience within the current level and marks that the track has experience.
         * @throws IllegalArgumentException if [exp] is negative.
         */
        public fun exp(exp: Double): Builder = apply {
            require(exp >= 0) { "'exp' should be non-negative, but it was $exp" }
            this.hasExp = true
            this.exp = exp
        }

        /**
         * Sets total experience required for the step from the current level to the next one.
         * Unknown by default. Use `0.0` if the next level is free. Requires [exp] to be set.
         * @throws IllegalArgumentException if [expToNextLevel] is negative.
         */
        public fun expToNextLevel(expToNextLevel: Double): Builder = apply {
            require(expToNextLevel >= 0) { "'expToNextLevel' should be non-negative, but it was $expToNextLevel" }
            this.expToNextLevel = expToNextLevel
        }

        /**
         * Builds [LevelProgress].
         * @throws IllegalStateException if [minLevel] is not set, [maxLevel] is lower than [minLevel],
         *   or [expToNextLevel] is set without [exp].
         */
        public fun build(): LevelProgress {
            check(minLevel >= 0) { "'minLevel' should be set" }
            check(maxLevel == -1 || maxLevel >= minLevel) {
                "'maxLevel' ($maxLevel) should not be lower than 'minLevel' ($minLevel)"
            }
            check(hasExp || expToNextLevel == -1.0) { "'expToNextLevel' requires 'exp' to be set" }
            return LevelProgressImpl(level, minLevel, maxLevel, hasExp, exp, expToNextLevel)
        }
    }

    public companion object {
        /**
         * Returns [Builder] of progress at the given [level].
         * @throws IllegalArgumentException if [level] is negative.
         */
        @JvmStatic
        public fun level(level: Int): Builder = Builder(level)
    }
}

private class LevelProgressImpl(
    override val level: Int,
    override val minLevel: Int,
    override val maxLevel: Int,
    private val hasExp: Boolean,
    override val exp: Double,
    override val expToNextLevel: Double,
) : LevelProgress {

    override fun hasExp(): Boolean = hasExp

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LevelProgress) return false
        return level == other.level &&
                minLevel == other.minLevel &&
                maxLevel == other.maxLevel &&
                hasExp == other.hasExp() &&
                exp == other.exp &&
                expToNextLevel == other.expToNextLevel
    }

    override fun hashCode(): Int {
        var result = level
        result = 31 * result + minLevel
        result = 31 * result + maxLevel
        result = 31 * result + hasExp.hashCode()
        result = 31 * result + exp.hashCode()
        result = 31 * result + expToNextLevel.hashCode()
        return result
    }

    override fun toString(): String =
        "LevelProgress(level=$level, minLevel=$minLevel, maxLevel=$maxLevel, " +
                "hasExp=$hasExp, exp=$exp, expToNextLevel=$expToNextLevel)"
}
