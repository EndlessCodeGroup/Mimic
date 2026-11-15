// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.resources

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

class ResourceStateTest {

    @Test
    fun `when create state - should keep values`() {
        val state = ResourceState.of(15.5, 20.0)

        state.amount shouldBe 15.5
        state.max shouldBe 20.0
    }

    @Test
    fun `when amount is above max - should keep it`() {
        ResourceState.of(25.0, 20.0).amount shouldBe 25.0
    }

    @Test
    fun `when values are negative - should replace them with zero`() {
        ResourceState.of(-3.0, -1.0) shouldBe ResourceState.of(0.0, 0.0)
    }

    @ParameterizedTest
    @ValueSource(doubles = [Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY])
    fun `when value is not finite - should throw`(value: Double) {
        shouldThrow<IllegalArgumentException> { ResourceState.of(value, 20.0) }
        shouldThrow<IllegalArgumentException> { ResourceState.of(10.0, value) }
    }
}
