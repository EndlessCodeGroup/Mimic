/*
 * This file is part of BukkitMimic.
 * Copyright (C) 2021 Osip Fatkullin
 * Copyright (C) 2021 EndlessCode Group and contributors
 *
 * BukkitMimic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BukkitMimic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BukkitMimic.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.endlesscode.mimic.impl.mimic

import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.ServiceRegistry
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.internal.MimicId
import ru.endlesscode.mimic.internal.authorsMention
import ru.endlesscode.mimic.internal.parseMimicId
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import java.util.concurrent.ConcurrentHashMap

/**
 * Items registry combining all items registries, using their IDs as namespaces: `acacia_boat` -> `minecraft:acacia_boat`.
 * ID without namespace or with the `mimic` namespace is searched over all registries.
 * Deprecated IDs with uppercase letters or more than one `:` are accepted with a warning.
 */
public class MimicItemsRegistry internal constructor(
    private val registries: ServiceRegistry<BukkitItemsRegistry>,
) : BukkitItemsRegistry {

    override val id: String = ID

    override val knownIds: Collection<String>
        get() = providers.flatMap { provider ->
            provider.tryGetKnownIds().mapNotNull { provider.namespaced(it) }
        }

    internal val providers: List<ItemsRegistryProvider>
        get() = registries.enabledEntries

    /** Keys of the warnings already logged, to log each warning once. */
    private val loggedWarnings = ConcurrentHashMap.newKeySet<String>()

    override fun isSameItem(item: ItemStack, itemId: String): Boolean {
        val id = parse(itemId) ?: return false
        return getRegistries(id)
            .any { provider -> provider.tryIsSameItem(item, id.path) }
    }

    override fun isItemExists(itemId: String): Boolean {
        val id = parse(itemId) ?: return false
        return getRegistries(id)
            .any { provider -> provider.tryIsItemExists(id.path) }
    }

    override fun getItemId(item: ItemStack): String? {
        val (provider, itemId) = providers.firstNotNullOfOrNull { provider ->
            provider.tryGetItemId(item)?.let { provider to it }
        } ?: return null
        return provider.namespaced(itemId)
    }

    override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? {
        val id = parse(itemId) ?: return null
        return getRegistries(id)
            .mapNotNull { service -> service.tryGetItem(id.path, payload, amount) }
            .firstOrNull()
    }

    private fun getRegistries(id: MimicId): Sequence<ItemsRegistryProvider> {
        return providers.asSequence()
            .filter { id.matches(it.registry.id) }
    }

    /** Parses the given [itemId], or returns `null` if it is invalid. */
    private fun parse(itemId: String): MimicId? {
        val lowercaseId = itemId.lowercase()
        val id = parseMimicId(lowercaseId, allowLegacy = true) ?: return null

        if (lowercaseId != itemId) {
            warnOnce("uppercase:$itemId") { "Item ID '$itemId' is deprecated, use '$lowercaseId' instead." }
        }
        if (id.isLegacy) {
            warnOnce("legacy") { "Item ID '$itemId' contains more than one ':', this is deprecated." }
        }
        return id
    }

    /** Returns the given [itemId] prefixed with the registry ID, or `null` if the result is invalid. */
    private fun ItemsRegistryProvider.namespaced(itemId: String): String? {
        val namespacedId = "${registry.id}:$itemId"
        val id = parseMimicId(namespacedId, allowLegacy = true)
        when {
            id == null -> warnOnce("invalid:${registry.id}") {
                "Items registry '${registry.id}' returned invalid ID '$itemId', such items are ignored. " +
                    "IDs may contain only a-z, 0-9, '.', '_', '-' and '/'. " +
                    "Please, report it to ${plugin.authorsMention}."
            }

            id.isLegacy -> warnOnce("legacy:${registry.id}") {
                "Items registry '${registry.id}' returned ID '$itemId' containing ':', this is deprecated. " +
                    "Use '/' instead: '${itemId.replace(':', '/')}'. " +
                    "Please, report it to ${plugin.authorsMention}."
            }
        }
        return namespacedId.takeIf { id != null }
    }

    private inline fun warnOnce(key: String, message: () -> String) {
        if (loggedWarnings.add(key)) Log.w(message())
    }

    public companion object {
        public const val ID: String = "mimic"
    }
}
