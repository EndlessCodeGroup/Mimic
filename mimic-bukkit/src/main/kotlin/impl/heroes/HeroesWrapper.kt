package ru.endlesscode.mimic.impl.heroes

import com.herocraftonline.heroes.Heroes
import com.herocraftonline.heroes.characters.Hero
import com.herocraftonline.heroes.util.Properties
import org.bukkit.entity.Player

internal class HeroesWrapper {

    val isEnabled: Boolean get() = Heroes.getInstance().isEnabled

    fun getHero(player: Player): Hero = Heroes.getInstance().characterManager.getHero(player)
    fun getExp(level: Int): Int = Properties.getExp(level)
    fun getTotalExp(level: Int): Int = Properties.getTotalExp(level)
    fun getLevel(exp: Double): Int = Properties.getLevel(exp)
}