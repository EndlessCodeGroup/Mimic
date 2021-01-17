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
            val hero = hero
            return (sequenceOf(
                hero.heroClass?.name,
                hero.secondaryClass?.name,
                hero.raceClass?.name,
            ) + hero.masteredClasses)
                .filterNotNull()
                .distinct()
                .toList()
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