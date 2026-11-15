// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.auraskills

import dev.aurelium.auraskills.api.AuraSkillsApi
import org.bukkit.Bukkit

internal class AuraSkillsWrapper {

    val isEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("AuraSkills")

    val api: AuraSkillsApi get() = AuraSkillsApi.get()
}
