package ru.endlesscode.mimic.command

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class IdHintsTest {

    private val ids = listOf(
        "minecraft:stone",
        "minecraft:stone_stairs",
        "craftengine:default/palm_log",
        "craftengine:default/palm_planks",
        "craftengine:default/trees/palm_sapling",
        "craftengine:internal/stone_button",
    )

    @ParameterizedTest
    @MethodSource("hints")
    fun `idHints - should suggest IDs, folders and namespaces`(input: String, expected: List<String>) {
        idHints(input, ids).map { it.text } shouldContainExactlyInAnyOrder expected
    }

    @ParameterizedTest
    @MethodSource("collapsedHints")
    fun `idHints - should collapse folders shared by all IDs`(input: String, expected: List<String>) {
        val ids = listOf(
            "minecraft:stone",
            "craftengine:default/trees/palm_sapling",
            "craftengine:default/trees/oak_sapling",
        )
        idHints(input, ids).map { it.text } shouldContainExactlyInAnyOrder expected
    }

    companion object {

        @JvmStatic
        fun collapsedHints(): Stream<Arguments> = Stream.of(
            arguments("cr", listOf("craftengine:default/trees/")),
            arguments("craftengine:", listOf("craftengine:default/trees/")),
            arguments("craftengine:d", listOf("craftengine:default/trees/palm_sapling", "craftengine:default/trees/oak_sapling", "craftengine:default/trees/")),
            arguments("minecraft:", listOf("minecraft:stone")),
        )

        @JvmStatic
        fun hints(): Stream<Arguments> = Stream.of(
            // Without namespace
            arguments(
                "stone",
                listOf("minecraft:stone", "minecraft:stone_stairs", "craftengine:internal/stone_button"),
            ),
            arguments("cr", listOf("craftengine:")),
            arguments("def", listOf("craftengine:default/palm_log", "craftengine:default/palm_planks", "craftengine:default/trees/palm_sapling", "craftengine:default/")),
            arguments(
                "default/",
                listOf("craftengine:default/palm_log", "craftengine:default/palm_planks", "craftengine:default/trees/"),
            ),
            // With namespace
            arguments("craftengine:", listOf("craftengine:default/", "craftengine:internal/")),
            arguments("craftengine:d", listOf("craftengine:default/palm_log", "craftengine:default/palm_planks", "craftengine:default/trees/palm_sapling", "craftengine:default/")),
            arguments(
                "craftengine:palm",
                listOf("craftengine:default/palm_log", "craftengine:default/palm_planks", "craftengine:default/trees/palm_sapling"),
            ),
            arguments(
                "craftengine:default/",
                listOf("craftengine:default/palm_log", "craftengine:default/palm_planks", "craftengine:default/trees/"),
            ),
            arguments("craftengine:default/t", listOf("craftengine:default/trees/palm_sapling", "craftengine:default/trees/")),
            arguments("craftengine:default/trees/", listOf("craftengine:default/trees/palm_sapling")),
            arguments("minecraft:", listOf("minecraft:stone", "minecraft:stone_stairs")),
            // Search over all namespaces
            arguments("mimic:def", listOf("craftengine:default/palm_log", "craftengine:default/palm_planks", "craftengine:default/trees/palm_sapling", "craftengine:default/")),
            // Unknown namespace
            arguments("craft:palm", listOf("craftengine:palm", "minecraft:palm")),
        )
    }
}
