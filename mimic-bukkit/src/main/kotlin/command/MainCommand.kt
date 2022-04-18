package ru.endlesscode.mimic.command

import co.aikar.commands.CommandHelp
import co.aikar.commands.MimicCommand
import co.aikar.commands.annotation.*
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.MimicService

@CommandAlias("%command")
@CommandPermission("%perm")
internal class MainCommand(
    private val plugin: Plugin,
    private val mimic: Mimic,
) : MimicCommand() {

    @HelpCommand
    @Description("Show help")
    fun doHelp(help: CommandHelp) {
        help.showHelp()
    }

    @OptIn(ExperimentalMimicApi::class)
    @Subcommand("info")
    @Description("Show info about Mimic and loaded services")
    fun info(sender: CommandSender) {
        val levelSystems = mimic.getAllLevelSystemProviders()
        val classSystems = mimic.getAllClassSystemProviders()
        val playerInventories = mimic.getAllPlayerInventoryProviders()
        val itemsRegistries = mimic.getAllItemsRegistries()

        sender.send(
            "&2${plugin.description.fullName}",
            "&3Level Systems: &7${levelSystems.toMessage()}",
            "&3Class Systems: &7${classSystems.toMessage()}",
            "&3Inventory Providers: &7${playerInventories.toMessage()}",
            "&3Items Registries: &7${itemsRegistries.toMessage()}",
        )
    }
}

private fun <ServiceT : MimicService> Map<String, ServiceT>.toMessage(): String {
    return values.joinToString { service ->
        val color = if (service.isEnabled) "&a" else "&c"
        "$color${service.id}&7"
    }
}
