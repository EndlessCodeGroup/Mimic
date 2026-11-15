// Copyright (c) 2021 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.heroes

import com.herocraftonline.heroes.Heroes
import com.herocraftonline.heroes.characters.Hero
import com.herocraftonline.heroes.characters.classes.HeroClass
import com.herocraftonline.heroes.util.Properties
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import ru.endlesscode.mimic.classes.ClassSystemSlotProvider

internal class HeroesWrapper {

    val isEnabled: Boolean get() = Heroes.getInstance().isEnabled

    fun getClasses(): Set<HeroClass> = Heroes.getInstance().classManager.classes

    /** Classes except the global skillset, which isn't a real class. */
    fun getRealClasses(): List<HeroClass> {
        val classManager = Heroes.getInstance().classManager
        val globalSkillset = classManager.globalSkillset
        return classManager.classes.filter { it != globalSkillset }
    }

    fun getHero(player: Player): Hero = Heroes.getInstance().characterManager.getHero(player)

    /** Returns the hero of the [player], or `null` if it isn't loaded. [getHero] loads or creates the hero. */
    fun getLoadedHero(player: Player): Hero? {
        val characterManager = Heroes.getInstance().characterManager
        return if (characterManager.containsHero(player)) characterManager.getHero(player) else null
    }

    fun callEvent(event: Event) = Bukkit.getPluginManager().callEvent(event)
    fun getExp(level: Int): Int = Properties.getExp(level)
    fun getTotalExp(level: Int): Int = Properties.getTotalExp(level)
    fun getLevel(exp: Double): Int = Properties.getLevel(exp)
}

/**
 * `true` if this is the default class or profession, which means "not chosen yet".
 * The default race is a real race, every hero has one.
 */
internal val HeroClass.isPlaceholder: Boolean
    get() = isDefault || isDefaultProf

/** Class name converted to an ID: `Dark Knight` becomes `dark_knight`. */
internal val HeroClass.classId: String
    get() = ClassSystemSlotProvider.toPath(name)
