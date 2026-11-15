// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.heroes

import com.herocraftonline.heroes.api.events.AfterClassChangeEvent
import com.herocraftonline.heroes.api.events.ClassChangeEvent
import com.herocraftonline.heroes.characters.Hero
import com.herocraftonline.heroes.characters.classes.HeroClass
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.classes.ClassProvider

/**
 * Heroes classes in slots `heroes:primary`, `heroes:secondary` (profession) and `heroes:race`.
 * The default class and profession are empty slots, the default race is a race. Both strong and weak parents are parents.
 * The class level is the track with the same ID as the class: `heroes:warrior`.
 */
internal class HeroesClassProvider(
    private val heroes: HeroesWrapper = HeroesWrapper(),
) : ClassProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = heroes.isEnabled

    override val allSlots: Collection<ClassProvider.Slot> = listOf(PRIMARY, SECONDARY, RACE)

    override val allClasses: Collection<ClassProvider.ClassDefinition>
        get() = selectableClasses().map { it.definition }

    override fun getClasses(player: Player): Map<String, ClassProvider.ClassDefinition> {
        val hero = player.loadedHero() ?: return emptyMap()
        return allSlots
            .mapNotNull { slot -> hero.classIn(slot)?.let { slot.id to it.definition } }
            .toMap()
    }

    override fun getPrimarySlot(player: Player): ClassProvider.Slot = PRIMARY

    override fun setClass(player: Player, classId: String, slotId: String?): Boolean {
        val hero = player.loadedHero() ?: return false
        val heroClass = selectableClasses().find { it.classId == classId } ?: return false
        val slots = heroClass.slots
        val slot = (if (slotId == null) slots.firstOrNull() else slots.find { it.id == slotId }) ?: return false

        val currentClass = hero.currentClassIn(slot)
        if (currentClass == heroClass) return true

        // The same as Heroes' `/hero admin class|prof|race`, the change can't be cancelled
        heroes.callEvent(ClassChangeEvent(hero, currentClass, heroClass, 0.0, false, false))
        hero.changeHeroClass(heroClass, slot == SECONDARY, slot == RACE)
        heroes.callEvent(AfterClassChangeEvent(hero, currentClass, heroClass, 0.0, false, false))
        return true
    }

    private fun Player.loadedHero(): Hero? = heroes.getLoadedHero(this)

    /** Classes except placeholders and the global skillset. */
    private fun selectableClasses(): List<HeroClass> = heroes.getRealClasses().filterNot { it.isPlaceholder }

    companion object {
        const val ID = "heroes"
    }
}

private val PRIMARY = ClassProvider.Slot("primary", Component.text("Class"))
private val SECONDARY = ClassProvider.Slot("secondary", Component.text("Profession"))
private val RACE = ClassProvider.Slot("race", Component.text("Race"))

/** The class in the [slot], or `null` if it is empty or holds a placeholder. */
private fun Hero.classIn(slot: ClassProvider.Slot): HeroClass? = currentClassIn(slot)?.takeUnless { it.isPlaceholder }

private fun Hero.currentClassIn(slot: ClassProvider.Slot): HeroClass? = when (slot) {
    PRIMARY -> heroClass
    SECONDARY -> secondaryClass
    else -> raceClass
}

/**
 * Slots this class can be held in, the preferred slot first.
 * Heroes makes classes primary by default, so primary is the last: a profession is usually primary too.
 */
private val HeroClass.slots: List<ClassProvider.Slot>
    get() = buildList {
        if (isRace) add(RACE)
        if (isSecondary) add(SECONDARY)
        if (isPrimary) add(PRIMARY)
    }

private val HeroClass.definition: ClassProvider.ClassDefinition
    get() = ClassProvider.ClassDefinition(
        classId,
        Component.text(name),
        // Placeholders aren't in allClasses
        parents = (strongParents + weakParents).filterNot { it.isPlaceholder }.mapTo(mutableSetOf()) { it.classId },
        levelTrack = classId,
    )
