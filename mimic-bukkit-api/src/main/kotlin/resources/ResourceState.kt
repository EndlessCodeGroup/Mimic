// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.resources

import org.jetbrains.annotations.ApiStatus

/**
 * Snapshot of a player's resource: how much they have and how much they can have.
 * The maximum is the one that applies to this player now, so it may differ between players.
 *
 * Values are read together and change all the time because of regeneration, so don't cache them.
 * States are equal if all their values are equal.
 *
 * Create it with [ResourceState.of]:
 * ```
 * ResourceState.of(15.0, 20.0);
 * ```
 * @since 0.10.0
 */
@ApiStatus.NonExtendable
public interface ResourceState {

    /** Current amount. It may be above [max] if the plugin allows it. */
    public val amount: Double

    /** The highest amount the player can have now. */
    public val max: Double

    public companion object {
        /**
         * Returns state with the given current [amount] and [max].
         * Negative values are replaced with zero, since some plugins let the amount go below it.
         * @throws IllegalArgumentException if [amount] or [max] is not finite.
         */
        @JvmStatic
        public fun of(amount: Double, max: Double): ResourceState {
            require(amount.isFinite()) { "'amount' should be finite, but it was $amount" }
            require(max.isFinite()) { "'max' should be finite, but it was $max" }
            return ResourceStateImpl(amount.coerceAtLeast(0.0), max.coerceAtLeast(0.0))
        }
    }
}

private class ResourceStateImpl(
    override val amount: Double,
    override val max: Double,
) : ResourceState {

    override fun equals(other: Any?): Boolean =
        other is ResourceState && amount == other.amount && max == other.max

    override fun hashCode(): Int = 31 * amount.hashCode() + max.hashCode()
    override fun toString(): String = "ResourceState(amount=$amount, max=$max)"
}
