// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.config

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.io.TempDir
import ru.endlesscode.mimic.BukkitTestBase
import java.io.File
import kotlin.test.Test

internal class MimicConfigTest : BukkitTestBase() {

    @TempDir
    lateinit var directory: File

    private val file: File get() = File(directory, "config.yml")

    private fun loadConfig() = MimicConfig("Mimic", file)

    @Test
    fun `when load disabled block providers - should remove default providers`() {
        file.writeText("disabled-block-providers: [custom, minecraft]\n")

        loadConfig().disabledBlockProviders shouldBe setOf("custom")
    }

    @Test
    fun `when load legacy disabled block registries option - should move it to disabled block providers`() {
        file.writeText("disabled-block-registries: [custom, minecraft]\n")

        loadConfig().disabledBlockProviders shouldBe setOf("custom")
        file.readText() shouldNotContain "disabled-block-registries"
    }

    @Test
    fun `when load legacy level system option - should move it to level provider`() {
        file.writeText("level-system: custom\n")

        loadConfig().levelProvider shouldBe "custom"
        file.readText() shouldNotContain "level-system"
    }

    @Test
    fun `when load both level options - should keep level provider`() {
        file.writeText("level-system: legacy\nlevel-provider: custom\n")

        loadConfig().levelProvider shouldBe "custom"
        file.readText() shouldNotContain "level-system"
    }

    @Test
    fun `when load legacy class system option - should move it to class provider`() {
        file.writeText("class-system: custom\n")

        loadConfig().classProvider shouldBe "custom"
        file.readText() shouldNotContain "class-system"
    }

    @Test
    fun `when reset string property - should save empty value`() {
        val config = loadConfig()
        config.levelProvider = "custom"

        config.reset(MimicConfig.LEVEL_PROVIDER)

        config.levelProvider.shouldBeEmpty()
        loadConfig().levelProvider.shouldBeEmpty()
    }

    @Test
    fun `when reset set property - should save empty set`() {
        val config = loadConfig()
        config.disabledBlockProviders = setOf("custom")

        config.reset(MimicConfig.DISABLED_BLOCK_PROVIDERS)

        config.disabledBlockProviders.shouldBeEmpty()
        loadConfig().disabledBlockProviders.shouldBeEmpty()
    }
}
