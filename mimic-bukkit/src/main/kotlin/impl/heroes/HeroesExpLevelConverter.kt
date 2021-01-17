package ru.endlesscode.mimic.impl.heroes

import ru.endlesscode.mimic.level.ExpLevelConverter

public class HeroesExpLevelConverter internal constructor(
    private val heroes: HeroesWrapper,
) : ExpLevelConverter {

    override fun expToFullLevel(exp: Double): Int = heroes.getLevel(exp)

    override fun expToLevel(exp: Double): Double {
        if (exp < 0) return 0.0

        val fullLevel = expToFullLevel(exp)
        val expDelta = exp - levelToExp(fullLevel)
        return fullLevel + expDelta / getExpToReachNextLevel(fullLevel)
    }

    override fun levelToExp(level: Int): Double = heroes.getTotalExp(level).toDouble()

    override fun getExpToReachLevel(level: Int): Double = heroes.getExp(level).toDouble()
}