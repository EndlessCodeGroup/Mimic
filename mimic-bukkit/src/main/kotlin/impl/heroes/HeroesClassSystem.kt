/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
 *
 * BukkitMimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BukkitMimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BukkitMimic.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.impl.heroes

import com.herocraftonline.heroes.characters.Hero
import org.bukkit.entity.Player
import ru.endlesscode.mimic.classes.BukkitClassSystem

public class HeroesClassSystem private constructor(
    player: Player,
    private val heroes: HeroesWrapper,
) : BukkitClassSystem(player) {

    public companion object {
        public const val ID: String = "heroes"
    }

    override val classes: List<String>
        get() {
            return heroes.getClasses()
                .filter { hero.getExperience(it) > 0 }
                .map { it.name }
        }

    override val primaryClass: String?
        get() = hero.heroClass?.name

    private val hero: Hero
        get() = heroes.getHero(player)

    internal class Provider : BukkitClassSystem.Provider(ID) {

        private val heroes = HeroesWrapper()

        override val isEnabled: Boolean
            get() = heroes.isEnabled

        override fun getSystem(player: Player): BukkitClassSystem {
            return HeroesClassSystem(player, heroes)
        }
    }
}
