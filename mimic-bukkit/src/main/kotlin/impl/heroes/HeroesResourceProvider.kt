// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.heroes

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.resources.ResourceProvider
import ru.endlesscode.mimic.resources.ResourceProvider.ResourceDefinition
import ru.endlesscode.mimic.resources.ResourceState

/** Heroes resources: the primary resource `heroes:mana` and `heroes:stamina`. Heroes stores them as whole numbers. */
internal class HeroesResourceProvider(
    private val heroes: HeroesWrapper = HeroesWrapper(),
) : ResourceProvider {

    override val id: String = ID
    override val isEnabled: Boolean get() = heroes.isEnabled

    override val allResources: Collection<ResourceDefinition> = listOf(MANA, STAMINA)

    override fun getPrimaryResource(player: Player): ResourceDefinition = MANA

    override fun getState(player: Player, resourceId: String): ResourceState? {
        val hero = heroes.getLoadedHero(player) ?: return null
        return when (resourceId) {
            MANA.id -> ResourceState.of(hero.mana.toDouble(), hero.maxMana.toDouble())
            STAMINA.id -> ResourceState.of(hero.stamina.toDouble(), hero.maxStamina.toDouble())
            else -> null
        }
    }

    override fun setAmount(player: Player, resourceId: String, amount: Double): Boolean {
        val hero = heroes.getLoadedHero(player) ?: return false
        // Both fire a change event, so other plugins can cancel or change it
        when (resourceId) {
            MANA.id -> hero.mana = amount.toInt()
            STAMINA.id -> hero.stamina = amount.toInt()
            else -> return false
        }
        return true
    }

    companion object {
        const val ID = "heroes"

        private val MANA = ResourceDefinition("mana", Component.text("Mana"))
        private val STAMINA = ResourceDefinition("stamina", Component.text("Stamina"))
    }
}
