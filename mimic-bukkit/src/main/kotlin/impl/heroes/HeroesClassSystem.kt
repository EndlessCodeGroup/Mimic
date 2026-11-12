// Copyright (c) 2021 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.heroes

import com.herocraftonline.heroes.characters.Hero
import org.bukkit.entity.Player
import ru.endlesscode.mimic.classes.BukkitClassSystem

public class HeroesClassSystem private constructor(
    player: Player,
    private val heroes: HeroesWrapper,
) : BukkitClassSystem(player) {

    override val classes: List<String>
        get() {
            return heroes.getClasses()
                .filter { hero.getExperience(it) > 0 }
                .map { it.name }
        }

    override val primaryClass: String
        get() = hero.heroClass.name

    private val hero: Hero
        get() = heroes.getHero(player)

    internal class Provider : BukkitClassSystem.Provider {
        private val heroes = HeroesWrapper()

        override val isEnabled: Boolean get() = heroes.isEnabled
        override val id: String = ID

        override fun getSystem(player: Player): BukkitClassSystem {
            return HeroesClassSystem(player, heroes)
        }
    }

    public companion object {
        public const val ID: String = "heroes"
    }
}
