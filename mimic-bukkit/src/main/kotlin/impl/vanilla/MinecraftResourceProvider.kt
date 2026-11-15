// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.vanilla

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.resources.ResourceProvider
import ru.endlesscode.mimic.resources.ResourceProvider.ResourceDefinition
import ru.endlesscode.mimic.resources.ResourceState

/**
 * Vanilla resources: health as `minecraft:health`, the food level as `minecraft:food`
 * and the remaining air as `minecraft:air`, in ticks.
 * There is no primary resource, so none of them is ever used as mana.
 *
 * Health is changed directly, as a cost and not as damage: no damage or regain events are fired,
 * armor and absorption hearts are ignored, and taking all health kills the player.
 */
internal class MinecraftResourceProvider : ResourceProvider {

    override val id: String = ID

    override val allResources: Collection<ResourceDefinition> = listOf(HEALTH, FOOD, AIR)

    override fun getPrimaryResource(player: Player): ResourceDefinition? = null

    override fun getState(player: Player, resourceId: String): ResourceState? = when (resourceId) {
        HEALTH.id -> ResourceState.of(player.health, player.maxHealthValue)
        FOOD.id -> ResourceState.of(player.foodLevel.toDouble(), MAX_FOOD_LEVEL)
        // The remaining air is negative while the player is drowning, the state shows it as zero
        AIR.id -> ResourceState.of(player.remainingAir.toDouble(), player.maximumAir.toDouble())
        else -> null
    }

    override fun setAmount(player: Player, resourceId: String, amount: Double): Boolean {
        when (resourceId) {
            // Health of a dead player must stay zero until respawn
            HEALTH.id -> if (player.isDead) return false else player.health = amount
            FOOD.id -> player.foodLevel = amount.toInt()
            AIR.id -> player.remainingAir = amount.toInt()
            else -> return false
        }
        return true
    }

    // The max health attribute was renamed in 1.21.3, the deprecated getter works on all versions
    @Suppress("DEPRECATION")
    private val Player.maxHealthValue: Double get() = maxHealth

    companion object {
        const val ID = "minecraft"

        private const val MAX_FOOD_LEVEL = 20.0
        private val HEALTH = ResourceDefinition("health", Component.text("Health"))
        private val FOOD = ResourceDefinition("food", Component.text("Food"))
        private val AIR = ResourceDefinition("air", Component.text("Air"))
    }
}
