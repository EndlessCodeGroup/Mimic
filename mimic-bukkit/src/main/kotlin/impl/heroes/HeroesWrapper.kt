package ru.endlesscode.mimic.impl.heroes

import com.herocraftonline.heroes.Heroes
import com.herocraftonline.heroes.characters.Hero
import org.bukkit.entity.Player

internal class HeroesWrapper {

    val isEnabled: Boolean get() = Heroes.getInstance().isEnabled

    fun getHero(player: Player): Hero {
        return Heroes.getInstance().characterManager.getHero(player)
    }
}