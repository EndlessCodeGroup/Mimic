// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.internal

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

internal class NormalizedIdsTest {

    private var nativeIds = listOf("ruby_sword", "Ruby Ore", "KingSword")
    private var nativeIdsRequests = 0

    // SUT
    private val ids = NormalizedIds(
        tag = "Tag",
        exists = { it in nativeIds },
        nativeIds = {
            nativeIdsRequests++
            nativeIds
        },
    )

    @Test
    fun `when get all ids - should return normalized ids`() {
        ids.all shouldContainExactly listOf("ruby_sword", "ruby_ore", "kingsword")
    }

    @Test
    fun `when get native id - should find it by normalized id`() {
        ids.nativeOf("ruby_sword") shouldBe "ruby_sword"
        ids.nativeOf("ruby_ore") shouldBe "Ruby Ore"
        ids.nativeOf("kingsword") shouldBe "KingSword"
    }

    @Test
    fun `when get native id by unknown or not normalized id - should return null`() {
        ids.nativeOf("ruby").shouldBeNull()
        ids.nativeOf("ruby_swords").shouldBeNull()
        ids.nativeOf("ruby ore").shouldBeNull()
        ids.nativeOf("kingSword").shouldBeNull()
    }

    @Test
    fun `when get normalized id - should return it for the native id`() {
        ids.normalizedOf("ruby_sword") shouldBe "ruby_sword"
        ids.normalizedOf("Ruby Ore") shouldBe "ruby_ore"
    }

    @Test
    fun `when native ids have the same normalized id - should use the exact one or the first one and warn once`() {
        val loggedWarnings = mutableListOf<String>()
        Log.init({ _, message, _ -> loggedWarnings += message.orEmpty() })
        nativeIds = listOf("Ruby_Sword", "ruby_sword", "Ruby Ore", "RUBY_ORE")

        repeat(2) { ids.all shouldContainExactly listOf("ruby_sword", "ruby_ore") }

        ids.nativeOf("ruby_sword") shouldBe "ruby_sword"
        ids.nativeOf("ruby_ore") shouldBe "Ruby Ore"
        ids.normalizedOf("Ruby_Sword").shouldBeNull()
        ids.normalizedOf("RUBY_ORE").shouldBeNull()
        loggedWarnings shouldContainExactly listOf(
            "[Tag] IDs [Ruby_Sword, ruby_sword] have the same normalized ID 'ruby_sword', only 'ruby_sword' is used.",
            "[Tag] IDs [Ruby Ore, RUBY_ORE] have the same normalized ID 'ruby_ore', only 'Ruby Ore' is used.",
        )
    }

    @Test
    fun `when get native id again - should not go through all native ids`() {
        repeat(2) { ids.nativeOf("kingsword") shouldBe "KingSword" }

        nativeIdsRequests shouldBe 1
    }

    @Test
    fun `when found native id doesn't exist anymore - should find native id again`() {
        ids.nativeOf("kingsword") shouldBe "KingSword"

        nativeIds = listOf("KINGSWORD")
        ids.nativeOf("kingsword") shouldBe "KINGSWORD"

        nativeIds = emptyList()
        ids.nativeOf("kingsword").shouldBeNull()
    }
}
