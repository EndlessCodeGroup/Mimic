// Copyright (c) 2021 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.mmoitems

import net.Indyuce.mmoitems.MMOItems
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

internal class MmoItemsWrapper {

    val isEnabled: Boolean get() = MMOItems.plugin.isEnabled
    val templates: Collection<MMOItemTemplate> get() = MMOItems.plugin.templates.collectTemplates()

    /** Returns name of the item type like `SWORD`. */
    fun getTypeName(template: MMOItemTemplate): String = template.type.id

    /** Returns name of the item type like `SWORD`, or `null` if the [item] isn't an MMOItems item. */
    fun getTypeName(item: ItemStack): String? = MMOItems.getTypeName(item)

    /** Returns ID of the [item] within its type, or `null` if the [item] isn't an MMOItems item. */
    fun getId(item: ItemStack): String? = MMOItems.getID(item)?.takeIf { it.isNotEmpty() }

    /**
     * Builds an item from the [template].
     * If the [player] is given, the item is built for them as MMOItems does, for example, scaled to their level.
     * Otherwise, the item has no tier and no level.
     */
    fun buildItem(template: MMOItemTemplate, player: Player?): ItemStack? {
        val builder = if (player != null) template.newBuilder(player) else template.newBuilder(0, null)
        return builder.build().newBuilder().build()
    }
}
