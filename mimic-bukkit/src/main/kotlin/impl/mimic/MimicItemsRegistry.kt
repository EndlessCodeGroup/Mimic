// Copyright (c) 2020 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("DEPRECATION") // ItemsRegistry API is deprecated but still supported

package ru.endlesscode.mimic.impl.mimic

import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.call
import ru.endlesscode.mimic.impl.mmoitems.MmoItemsItemProvider
import ru.endlesscode.mimic.internal.MimicId
import ru.endlesscode.mimic.internal.OnceWarnings
import ru.endlesscode.mimic.internal.parseMimicId
import ru.endlesscode.mimic.items.BukkitItemsRegistry
import ru.endlesscode.mimic.items.ItemsRegistryItemProvider

/**
 * Items registry showing items of all item providers via the deprecated ItemsRegistry API,
 * using provider IDs as namespaces: `acacia_boat` -> `minecraft:acacia_boat`.
 *
 * Unlike Items API, it accepts IDs deprecated there:
 * - ID without namespace or with the `mimic` namespace is searched over all providers.
 * - IDs with uppercase letters or more than one `:` are accepted with a warning.
 * - MMOItems ID without item type (`mmoitems:excalibur`) is accepted with a warning if only one type has this item.
 */
@Deprecated("Use Items API instead.")
public class MimicItemsRegistry internal constructor(private val items: MimicItems) : BukkitItemsRegistry {

    override val id: String = ID

    override val knownIds: Collection<String>
        get() = items.getAllItems().map { it.asString() }

    private val warnings: OnceWarnings
        get() = items.providers.registry.warnings

    override fun isSameItem(item: ItemStack, itemId: String): Boolean {
        val (owner, ownerId) = items.owner(item) ?: return false
        return candidates(itemId).any { (provider, path) ->
            provider == owner && (path == ownerId.value() || provider.isSameItemInRegistry(item, path))
        }
    }

    override fun isItemExists(itemId: String): Boolean =
        candidates(itemId).any { (provider, path) -> provider.hasItem(path) }

    override fun getItemId(item: ItemStack): String? = items.getItemId(item)?.asString()

    override fun getItem(itemId: String, payload: Any?, amount: Int): ItemStack? =
        candidates(itemId).firstNotNullOfOrNull { (provider, path) -> provider.getItem(path, payload, amount) }

    /** Returns providers that may have the item with the given [itemId], paired with the ID of the item in the provider. */
    private fun candidates(itemId: String): List<Pair<ItemProviderEntry, String>> {
        val id = parse(itemId) ?: return emptyList()
        val path = id.path.replace(':', '/')
        return items.providers.enabled
            .filter { id.matches(it.service.id) }
            .map { provider -> provider to provider.withMmoItemsType(path) }
    }

    /** Parses the given [itemId], or returns `null` if it is invalid. */
    private fun parse(itemId: String): MimicId? {
        val lowercaseId = itemId.lowercase()
        val id = parseMimicId(lowercaseId, allowLegacy = true) ?: return null

        if (lowercaseId != itemId) {
            warnings.warn("uppercase:$itemId") { "Item ID '$itemId' is deprecated, use '$lowercaseId' instead." }
        }
        if (id.isLegacy) {
            warnings.warn("legacy:$itemId") {
                "Item ID '$itemId' contains more than one ':', this is deprecated. Use '/' instead of the next ones."
            }
        }
        return id
    }

    /**
     * MMOItems IDs had no item type before Mimic 0.11: `mmoitems:excalibur` instead of `mmoitems:sword/excalibur`.
     * Returns the [path] with the type if this is MMOItems provider and only one type has the item.
     */
    private fun ItemProviderEntry.withMmoItemsType(path: String): String {
        if (service.id != MmoItemsItemProvider.ID || '/' in path) return path
        val paths = call(default = emptyList()) { allItems.filter { it.substringAfter('/') == path } }
        when (paths.size) {
            0 -> Unit
            1 -> warnings.warn("mmoitems-type:$path") {
                "Item ID '${service.id}:$path' without item type is deprecated, use '${service.id}:${paths.single()}' instead."
            }

            else -> warnings.warn("mmoitems-type:$path") {
                "Item ID '${service.id}:$path' is ambiguous, specify item type: ${paths.joinToString { "'${service.id}:$it'" }}."
            }
        }
        return paths.singleOrNull() ?: path
    }

    /** Asks the registry behind this provider to compare items, so its own rules still work, like ignoring a part of the ID. */
    private fun ItemProviderEntry.isSameItemInRegistry(item: ItemStack, path: String): Boolean {
        val registry = service as? ItemsRegistryItemProvider ?: return false
        return call(default = false) { registry.isSameItem(item, path) }
    }

    /** Payload may be of any type in ItemsRegistry API, but only registries may take a payload which isn't a string. */
    private fun ItemProviderEntry.getItem(path: String, payload: Any?, amount: Int): ItemStack? {
        val registry = service as? ItemsRegistryItemProvider
        return when {
            payload == null || payload is String -> createItem(path, amount, player = null, payload as String?)
            registry != null -> call(default = null) { registry.getItem(path, payload, amount) }
            else -> {
                warnings.warn("payload-type:${service.id}:${payload.javaClass.name}") {
                    "$apiName '${service.id}' takes only string payloads, but got ${payload.javaClass.name}. " +
                        "Items with such payloads aren't created."
                }
                null
            }
        }
    }

    public companion object {
        public const val ID: String = "mimic"
    }
}
