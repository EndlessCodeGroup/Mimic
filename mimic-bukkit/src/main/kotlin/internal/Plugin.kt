// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.internal

import org.bukkit.plugin.Plugin

/** Mention of the plugin authors for messages, for example `MyPlugin authors (Alice, Bob)`. */
internal val Plugin.authorsMention: String
    get() {
        @Suppress("UnstableApiUsage")
        val authors = pluginMeta.authors
        return if (authors.isEmpty()) "$name authors" else "$name authors (${authors.joinToString()})"
    }
