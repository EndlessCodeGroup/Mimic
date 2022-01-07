package ru.endlesscode.mimic.impl.mmoitems

import io.lumine.mythic.lib.api.item.NBTItem
import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.manager.TemplateManager
import org.bukkit.inventory.ItemStack

internal class MmoItemsWrapper {

    val isEnabled: Boolean get() = MMOItems.plugin.isEnabled
    val templatesManager: TemplateManager get() = MMOItems.plugin.templates

    fun getNbtItem(item: ItemStack): NBTItem = NBTItem.get(item)
}
