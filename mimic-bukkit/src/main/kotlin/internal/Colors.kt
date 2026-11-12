// Copyright (c) 2021 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // Keep it for now

package ru.endlesscode.mimic.internal

import org.bukkit.ChatColor

internal fun String.stripColor(): String = checkNotNull(ChatColor.stripColor(this))
