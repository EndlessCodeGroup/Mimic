// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.internal

import net.kyori.adventure.key.Key
import java.util.concurrent.ConcurrentHashMap

/**
 * Converts native IDs of a plugin to normalized IDs and back.
 * Native IDs are lowercased and characters not allowed in Mimic IDs are replaced with `_`: `Ruby Ore` -> `ruby_ore`.
 * If several native IDs have the same normalized ID, the one equal to the normalized ID is used,
 * otherwise the first one.
 *
 * @param tag Name of the plugin for warnings.
 * @param exists Checks that the given native ID exists.
 * @param nativeIds Returns all native IDs.
 */
internal class NormalizedIds(
    private val tag: String,
    private val exists: (String) -> Boolean,
    private val nativeIds: () -> Collection<String>,
) {

    private val warnings = OnceWarnings()

    /** Native IDs found by normalized IDs. An entry may be outdated, so it should be checked before use. */
    private val foundNativeIds = ConcurrentHashMap<String, String>()

    /** All normalized IDs. */
    val all: List<String>
        get() {
            val nativeIdsByNormalized = nativeIds().groupBy(::normalize)
            for ((normalizedId, sameIds) in nativeIdsByNormalized) {
                if (sameIds.size > 1) {
                    warnings.warn(normalizedId) {
                        "[$tag] IDs $sameIds have the same normalized ID '$normalizedId', " +
                            "only '${nativeOf(normalizedId)}' is used."
                    }
                }
            }
            return nativeIdsByNormalized.keys.toList()
        }

    /** Returns normalized ID of the given [nativeId], or `null` if another native ID is used for this normalized ID. */
    fun normalizedOf(nativeId: String): String? = normalize(nativeId).takeIf { nativeOf(it) == nativeId }

    /** Returns native ID for the given [normalizedId], or `null` if there is no such ID. */
    fun nativeOf(normalizedId: String): String? {
        if (exists(normalizedId)) return normalizedId
        foundNativeIds[normalizedId]?.let { if (exists(it)) return it }

        val nativeId = nativeIds().find { it.normalizesTo(normalizedId) }
        if (nativeId != null) foundNativeIds[normalizedId] = nativeId else foundNativeIds.remove(normalizedId)
        return nativeId
    }

    companion object {
        /** Normalizes the given [id]: lowercases it and replaces characters not allowed in Mimic IDs with `_`. */
        fun normalize(id: String): String = buildString(id.length) {
            for (char in id) append(normalize(char))
        }

        private fun normalize(char: Char): Char = char.lowercaseChar().let { if (Key.allowedInValue(it)) it else '_' }

        /** Checks that [normalizedId] is the normalized form of this ID. Doesn't allocate, unlike [normalize]. */
        private fun String.normalizesTo(normalizedId: String): Boolean {
            if (length != normalizedId.length) return false
            for (index in indices) {
                if (normalize(this[index]) != normalizedId[index]) return false
            }
            return true
        }
    }
}
