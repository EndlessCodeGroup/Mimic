package ru.endlesscode.mimic.impl.heroes

import com.herocraftonline.heroes.characters.Hero
import org.bukkit.entity.Player
import ru.endlesscode.mimic.level.BukkitLevelSystem
import ru.endlesscode.mimic.level.ExpLevelConverter

public class HeroesLevelSystem private constructor(
    player: Player,
    private val heroes: HeroesWrapper,
) : BukkitLevelSystem(player) {

    override val converter: ExpLevelConverter = HeroesExpLevelConverter(heroes)

    override var level: Int
        get() = hero.getHeroLevel(hero.heroClass)
        set(value) {
            val expDelta = converter.levelToExp(value) - converter.levelToExp(level)
            giveExp(expDelta)
        }

    override var totalExp: Double
        get() = hero.getExperience(hero.heroClass)
        set(value) {
            val expDelta = value - totalExp
            giveExp(expDelta)
        }

    override var exp: Double
        get() = totalExp - converter.levelToExp(level)
        set(value) {
            val expDelta = (value - exp).coerceIn(0.0, expToNextLevel)
            giveExp(expDelta)
        }

    override fun takeExp(expAmount: Double) {
        giveExp(-expAmount)
    }

    override fun giveExp(expAmount: Double) {
        val remainingExpAmount = converter.levelToExp(hero.heroClass.maxLevel) - totalExp
        hero.addExp(expAmount.coerceAtMost(remainingExpAmount), hero.heroClass, player.location)
    }

    private val hero: Hero
        get() = heroes.getHero(player)

    internal class Provider : BukkitLevelSystem.Provider {
        private val heroes = HeroesWrapper()

        override val isEnabled: Boolean get() = heroes.isEnabled
        override val id: String = ID

        override fun getSystem(player: Player): BukkitLevelSystem {
            return HeroesLevelSystem(player, heroes)
        }
    }

    public companion object {
        public const val ID: String = "heroes"
    }
}
