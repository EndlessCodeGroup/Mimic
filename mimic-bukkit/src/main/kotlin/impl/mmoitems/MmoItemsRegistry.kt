package ru.endlesscode.mimic.impl.mmoitems

import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate
import org.bukkit.inventory.ItemStack
import ru.endlesscode.mimic.items.BukkitItemsRegistry

public class MmoItemsRegistry private constructor(private val mmoItems: MmoItemsWrapper) : BukkitItemsRegistry {

    public companion object {
        public const val ID: String = "mmoitems"
    }

    internal constructor() : this(MmoItemsWrapper())

    override val isEnabled: Boolean
        get() = mmoItems.isEnabled

    override val id: String = ID

    override val knownIds: Collection<String>
        get() = templates.map { it.id.toLowerCase() }

    private val templates: Collection<MMOItemTemplate>
        get() = mmoItems.templatesManager.collectTemplates()

    override fun isItemExists(itemId: String): Boolean = itemId in knownIds

    override fun getItemId(item: ItemStack): String? {
        val nbtItem = mmoItems.getNbtItem(item)
        return nbtItem.getString("MMOITEMS_ITEM_ID")?.toLowerCase()
    }

    override fun getItem(itemId: String, amount: Int): ItemStack? {
        val template = templates.find { it.id.equals(itemId, ignoreCase = true) } ?: return null

        return template.buildItemStack()
            .also { it.amount = amount.coerceIn(1, it.maxStackSize) }
    }

    private fun MMOItemTemplate.buildItemStack(): ItemStack {
        return newBuilder(0, null)
            .build()
            .newBuilder()
            .build()
    }
}