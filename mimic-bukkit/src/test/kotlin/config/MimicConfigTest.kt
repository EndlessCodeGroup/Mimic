package ru.endlesscode.mimic.config

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
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
    fun `when load disabled block registries - should remove default registries`() {
        file.writeText("disabled-block-registries: [custom, minecraft, mimic]\n")

        loadConfig().disabledBlockRegistries shouldBe setOf("custom")
    }

    @Test
    fun `when reset string property - should save empty value`() {
        val config = loadConfig()
        config.levelSystem = "custom"

        config.reset(MimicConfig.LEVEL_SYSTEM)

        config.levelSystem.shouldBeEmpty()
        loadConfig().levelSystem.shouldBeEmpty()
    }

    @Test
    fun `when reset set property - should save empty set`() {
        val config = loadConfig()
        config.disabledBlockRegistries = setOf("custom")

        config.reset(MimicConfig.DISABLED_BLOCK_REGISTRIES)

        config.disabledBlockRegistries.shouldBeEmpty()
        loadConfig().disabledBlockRegistries.shouldBeEmpty()
    }
}
