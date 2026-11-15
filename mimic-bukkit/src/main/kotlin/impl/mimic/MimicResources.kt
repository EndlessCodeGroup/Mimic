// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mimic

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.ServiceEntry
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.call
import ru.endlesscode.mimic.impl.vanilla.MinecraftResourceProvider
import ru.endlesscode.mimic.resources.Resource
import ru.endlesscode.mimic.resources.ResourceProvider
import ru.endlesscode.mimic.resources.ResourceState
import ru.endlesscode.mimic.resources.Resources

internal typealias ResourceProviderEntry = ServiceEntry<ResourceProvider>

/**
 * Resources combining resources of all resource providers, using provider IDs as namespaces: `mana` -> `mmocore:mana`.
 *
 * Providers only read the state and set the amount. Taking and giving are done here,
 * so the checks and the results are the same for all plugins.
 * The amount passed to providers is never negative, and is above the maximum only on a take from an amount above it.
 * The primary resource is taken from the provider with [preferredProviderId].
 * If it isn't set, the primary resource is taken from the first provider in order of priority that has one for the player.
 */
internal class MimicResources(
    resourceProviders: ServiceRegistry<ResourceProvider>,
    preferredProviderId: () -> String,
) : Resources {

    val providers = NamespacedProviders(resourceProviders, "primary resource", preferredProviderId)

    override fun getAllResources(): List<Resource> =
        providers.enabled.flatMap { provider ->
            // Resources are converted inside call, so a provider returning null resources from Java is skipped
            provider.call(default = emptyList()) {
                provider.validValues(allResources, RESOURCE) { it.id }.map { provider.resource(it) }
            }
        }

    override fun getPrimaryResource(player: Player): Resource? {
        if (!player.isOnline) return null
        return providers.selectPrimary { it.primaryResource(player) }
    }

    override fun getState(player: Player, resourceId: Key): ResourceState? {
        if (!player.isOnline) return null
        val provider = findProvider(resourceId) ?: return null
        return provider.state(player, resourceId)
    }

    override fun has(player: Player, resourceId: Key, amount: Double): Boolean {
        requireValidAmount(amount)
        val state = getState(player, resourceId) ?: return false
        return state.amount >= amount
    }

    override fun take(player: Player, resourceId: Key, amount: Double): Boolean {
        requireValidAmount(amount)
        return change(player, resourceId) { provider, state ->
            if (state.amount < amount) return false
            if (amount == 0.0) return true
            if (!provider.setAmount(player, resourceId, state.amount - amount)) return false

            // Plugins don't report that the change was cancelled, so check that something was taken.
            // The taken amount may differ: plugins storing whole numbers round it, others apply discounts.
            // If the state can't be read anymore, the change was requested and nothing says it was cancelled.
            val newState = provider.state(player, resourceId) ?: return true
            newState.amount < state.amount
        }
    }

    override fun give(player: Player, resourceId: Key, amount: Double): Boolean {
        requireValidAmount(amount)
        return change(player, resourceId) { provider, state ->
            // The amount may already be above the maximum, then giving must not lower it
            val newAmount = (state.amount + amount).coerceAtMost(state.max)
            newAmount <= state.amount || provider.setAmount(player, resourceId, newAmount)
        }
    }

    override fun set(player: Player, resourceId: Key, amount: Double): Boolean {
        require(amount.isFinite()) { "'amount' should be finite, but it was $amount" }
        return change(player, resourceId) { provider, state ->
            provider.setAmount(player, resourceId, amount.coerceIn(0.0, state.max))
        }
    }

    /** Runs the [block] with the provider of the resource and its current state, returns `false` if there is none. */
    private inline fun change(
        player: Player,
        resourceId: Key,
        block: (provider: ResourceProviderEntry, state: ResourceState) -> Boolean,
    ): Boolean {
        if (!player.isOnline) return false
        val provider = findProvider(resourceId) ?: return false
        val state = provider.state(player, resourceId) ?: return false
        return block(provider, state)
    }

    private fun ResourceProviderEntry.state(player: Player, resourceId: Key): ResourceState? =
        call(default = null) { getState(player, resourceId.value()) }

    private fun ResourceProviderEntry.setAmount(player: Player, resourceId: Key, amount: Double): Boolean =
        call(default = false) { setAmount(player, resourceId.value(), amount) }

    private fun ResourceProviderEntry.primaryResource(player: Player): Resource? {
        val definition = call(default = null) { getPrimaryResource(player) } ?: return null
        return if (isValidPath(definition.id, RESOURCE)) resource(definition) else null
    }

    private fun findProvider(resourceId: Key): ResourceProviderEntry? =
        providers.find(resourceId, RESOURCE) { path -> call(default = false) { allResources.any { it.id == path } } }

    private fun requireValidAmount(amount: Double) {
        require(amount >= 0 && amount.isFinite()) { "'amount' should be non-negative and finite, but it was $amount" }
    }

    private fun ResourceProviderEntry.resource(definition: ResourceProvider.ResourceDefinition): Resource =
        MimicResource(key(definition.id), definition.name)

    private companion object {
        const val RESOURCE = "resource"
    }
}

private class MimicResource(
    private val key: Key,
    private val name: Component,
) : Resource {
    override fun key(): Key = key
    override fun name(): Component = name
    override fun equals(other: Any?): Boolean = other is Resource && key == other.key()
    override fun hashCode(): Int = key.hashCode()
    override fun toString(): String = "Resource(${key.asString()})"
}

/**
 * IDs of registered resource providers, the values of `resource-provider` config option.
 * The default provider is skipped: it has no primary resource, so selecting it would leave players without one.
 */
internal fun Mimic.getResourceProviderIds(): Set<String> =
    (resources as MimicResources).providers.allIds - MinecraftResourceProvider.ID

/** ID of the provider of the primary resource, or `null` if there is only the default provider, which has none. */
internal fun Mimic.getPrimaryResourceProviderId(): String? =
    (resources as MimicResources).providers.primary?.service?.id.takeIf { it != MinecraftResourceProvider.ID }
