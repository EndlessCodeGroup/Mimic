package ru.endlesscode.mimic.impl.quantumrpg

import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.items.BukkitItemsRegistry

public class QuantumRpgItemsRegistry private constructor(
    private val quantumRpg: QuantumRpgWrapper,
) : BukkitItemsRegistry {

    public companion object {
        public const val ID: String = "quantumrpg"

        private const val SEPARATOR = '/'
    }

    internal constructor() : this(QuantumRpgWrapper())

    override val isEnabled: Boolean get() = quantumRpg.isEnabled
    override val id: String get() = ID

    override val knownIds: Collection<String>
        get() = quantumRpg.itemsModules
            .map { module -> module.itemIds.asSequence().map { module.namespaced(it) } }
            .flatten()
            .toList()

    override fun isSameItem(item: ItemStack, itemId: String): Boolean {
        val realItemId = getItemId(item) ?: return false
        return itemId == realItemId || itemId == realItemId.split(SEPARATOR, limit = 2).last()
    }

    override fun isItemExists(itemId: String): Boolean {
        return runOnModules(itemId) { id ->
            any { it.getItemById(id) != null }
        }
    }

    override fun getItemId(item: ItemStack): String? {
        return quantumRpg.itemsModules
            .mapNotNull { module -> module.getItemId(item)?.let { module.namespaced(it) } }
            .firstOrNull()
    }

    override fun getItem(itemId: String, amount: Int): ItemStack? {
        return runOnModules(itemId) { id ->
            mapNotNull { module -> module.getItemById(id) }
                .map { it.create() }
                .firstOrNull()
        }?.also { it.amount = amount.coerceIn(0, it.maxStackSize) }
    }

    private fun <T> runOnModules(itemId: String, block: Sequence<DropModule>.(id: String) -> T): T {
        val (namespace, id) = if (SEPARATOR in itemId) {
            itemId.split(SEPARATOR, limit = 2)
        } else {
            listOf("", itemId)
        }

        return quantumRpg.itemsModules
            .filter { it.namespaceMatches(namespace) }
            .block(id)
    }

    private fun DropModule.namespaced(itemId: String) = "$id$SEPARATOR$itemId"
    private fun DropModule.namespaceMatches(namespace: String) = namespace.isEmpty() || namespace == id
}
