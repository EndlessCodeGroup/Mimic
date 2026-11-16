// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.impl.executableitems

import com.ssomar.score.api.executableitems.ExecutableItemsAPI
import com.ssomar.score.api.executableitems.config.ExecutableItemsManagerInterface
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

/** Wraps ExecutableItems static API, which is a part of SCore. All IDs are ExecutableItems IDs. */
internal class ExecutableItemsWrapper {

    val isEnabled: Boolean get() = Bukkit.getPluginManager().isPluginEnabled("ExecutableItems")

    private val manager: ExecutableItemsManagerInterface get() = ExecutableItemsAPI.getExecutableItemsManager()

    val itemIds: List<String> get() = manager.executableItemIdsList

    fun hasItem(id: String): Boolean = manager.isValidID(id)
    fun getItemId(item: ItemStack): String? = manager.getExecutableItem(item).getOrNull()?.id

    /** Creates item with the given [amount]. The [player] becomes the owner of the item if it is given. */
    fun createItem(id: String, amount: Int, player: Player?): ItemStack? =
        manager.getExecutableItem(id).getOrNull()?.buildItem(amount, Optional.ofNullable(player))
}
