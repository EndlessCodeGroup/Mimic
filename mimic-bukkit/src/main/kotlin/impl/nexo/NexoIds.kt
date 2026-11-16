// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.nexo

import net.kyori.adventure.key.Key
import ru.endlesscode.mimic.internal.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Converts Nexo IDs to Mimic IDs and back.
 * Nexo IDs are lowercased and characters not allowed in Mimic IDs are replaced with `_`: `Ruby Ore` -> `ruby_ore`.
 * If several Nexo IDs become the same Mimic ID, the one equal to the Mimic ID is used, otherwise the first one.
 *
 * @param exists Checks that the given Nexo ID exists.
 * @param nexoIds Returns all Nexo IDs.
 */
internal class NexoIds(
    private val exists: (String) -> Boolean,
    private val nexoIds: () -> List<String>,
) {

    private val reportedDuplicates = ConcurrentHashMap.newKeySet<String>()

    val mimicIds: List<String>
        get() {
            val idsByMimicId = nexoIds().groupBy(String::toMimicId)
            for ((mimicId, sameIds) in idsByMimicId) {
                if (sameIds.size > 1 && reportedDuplicates.add(mimicId)) {
                    val usedId = if (mimicId in sameIds) mimicId else sameIds.first()
                    Log.w("[Nexo] IDs $sameIds have the same Mimic ID '$mimicId', only '$usedId' is used.")
                }
            }
            return idsByMimicId.keys.toList()
        }

    /** Returns Nexo ID for the given [mimicId], or `null` if there is no such ID. */
    fun findNexoId(mimicId: String): String? =
        mimicId.takeIf(exists) ?: nexoIds().find { it.toMimicId() == mimicId }
}

internal fun String.toMimicId(): String = buildString {
    for (char in this@toMimicId.lowercase()) append(if (Key.allowedInValue(char)) char else '_')
}
