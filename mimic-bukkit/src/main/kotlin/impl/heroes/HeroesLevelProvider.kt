// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.heroes

import com.herocraftonline.heroes.characters.Hero
import com.herocraftonline.heroes.characters.classes.HeroClass
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.level.LevelProgress
import ru.endlesscode.mimic.level.LevelProvider

/**
 * Heroes classes as tracks with class IDs: `heroes:warrior`. Heroes keeps experience of every class, so each class is a track.
 * The primary track is the hero's primary class.
 */
internal class HeroesLevelProvider(
    private val heroes: HeroesWrapper = HeroesWrapper(),
) : LevelProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = heroes.isEnabled

    override val allTracks: Collection<LevelProvider.Track>
        get() = heroes.getRealClasses().map { it.track }

    override fun getTracks(player: Player): Collection<LevelProvider.Track> =
        player.loadedHero()?.ownedClasses()?.map { it.track }.orEmpty()

    override fun getPrimaryTrack(player: Player): LevelProvider.Track? = player.loadedHero()?.heroClass?.track

    override fun getProgress(player: Player, trackId: String): LevelProgress? {
        val hero = player.loadedHero() ?: return null
        val heroClass = hero.findClass(trackId) ?: return null
        val level = hero.levelOf(heroClass)
        val builder = LevelProgress.level(level)
            .minLevel(MIN_LEVEL)
            .maxLevel(heroClass.maxLevel)
            .exp((hero.getExperience(heroClass) - getTotalExp(level)).coerceAtLeast(0.0))
        if (level < heroClass.maxLevel) builder.expToNextLevel((getTotalExp(level + 1) - getTotalExp(level)).toDouble())
        return builder.build()
    }

    override fun setLevel(player: Player, trackId: String, level: Int): Boolean {
        val hero = player.loadedHero() ?: return false
        val heroClass = hero.findClass(trackId) ?: return false

        // The same as Heroes' level command: addExp changes the level, fires events and sends messages
        val targetExp = getTotalExp(level).toDouble()
        hero.addExp(targetExp - hero.getExperience(heroClass), heroClass, player.location)
        // Floating point may leave a tiny difference
        if (hero.getExperience(heroClass) != targetExp) {
            hero.setExperience(heroClass, targetExp)
            hero.syncExperience()
        }
        return true
    }

    override fun giveExp(player: Player, trackId: String, amount: Double): Boolean {
        val hero = player.loadedHero() ?: return false
        val heroClass = hero.findClass(trackId) ?: return false

        // addExp ignores the change if it exceeds the max level, so give at most the experience left to it
        val maxExp = getTotalExp(heroClass.maxLevel) - hero.getExperience(heroClass)
        val exp = amount.coerceAtMost(maxExp)
        if (exp > 0) hero.addExp(exp, heroClass, player.location)
        return true
    }

    override fun takeExp(player: Player, trackId: String, amount: Double): Boolean {
        val hero = player.loadedHero() ?: return false
        val heroClass = hero.findClass(trackId) ?: return false

        // Heroes lowers levels itself and keeps experience non-negative
        hero.addExp(-amount, heroClass, player.location)
        return true
    }

    private fun Player.loadedHero(): Hero? = heroes.getLoadedHero(this)

    /** Total experience to reach the [level], the curve is the same for all classes. */
    private fun getTotalExp(level: Int): Int = heroes.getTotalExp(level)

    private fun Hero.ownedClasses(): List<HeroClass> = heroes.getRealClasses().filter { owns(it) }

    private fun Hero.findClass(trackId: String): HeroClass? =
        heroes.getRealClasses().find { it.classId == trackId }?.takeIf { owns(it) }

    companion object {
        const val ID = "heroes"
    }
}

private const val MIN_LEVEL = 1

/** Level of the [heroClass] within its limits. Heroes may return levels out of them for invalid experience. */
private fun Hero.levelOf(heroClass: HeroClass): Int = getHeroLevel(heroClass).coerceIn(MIN_LEVEL, heroClass.maxLevel)

/** `true` if the hero has the [heroClass] now or has saved experience of it. */
private fun Hero.owns(heroClass: HeroClass): Boolean =
    heroClass == this.heroClass || heroClass == secondaryClass || heroClass == raceClass ||
        experienceMap.keys.any { it.equals(heroClass.name, ignoreCase = true) }

private val HeroClass.track: LevelProvider.Track
    get() = LevelProvider.Track(classId, Component.text(name))
