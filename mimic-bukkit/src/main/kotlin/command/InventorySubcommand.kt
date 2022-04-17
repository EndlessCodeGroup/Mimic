package ru.endlesscode.mimic.command

import co.aikar.commands.MimicCommand
import co.aikar.commands.annotation.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.Mimic

@OptIn(ExperimentalMimicApi::class)
@CommandAlias("%command")
@CommandPermission("%perm")
@Subcommand("inventory")
internal class InventorySubcommand(private val mimic: Mimic) : MimicCommand() {

    @Subcommand("info")
    @Description("Show information about player's inventory provider")
    @CommandCompletion("@players")
    fun info(sender: CommandSender, @Optional @Flags("other,defaultself") player: Player?) {
        if (player == null) return
        val provider = mimic.getPlayerInventoryProvider()
        val inventory = provider.getSystem(player)
        sender.send(
            "&3Inventory provider: &7${provider.id}",
            "&3Count of Equipped: &7%d".format(inventory.equippedItems.size),
            "&3Count of Stored: &7%d".format(inventory.storedItems.size),
        )
    }
}
