package ru.endlesscode.mimic.bukkit.command

import co.aikar.commands.CommandHelp
import co.aikar.commands.MimicCommand
import co.aikar.commands.annotation.*
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.MimicService
import ru.endlesscode.mimic.bukkit.BukkitClassSystem
import ru.endlesscode.mimic.bukkit.BukkitItemsRegistry
import ru.endlesscode.mimic.bukkit.BukkitLevelSystem
import ru.endlesscode.mimic.bukkit.loadAll

@CommandAlias("%command")
@CommandPermission("%perm")
internal class MainCommand(private val plugin: Plugin) : MimicCommand() {

    @HelpCommand
    @Description("Show help")
    fun doHelp(help: CommandHelp) {
        help.showHelp()
    }

    @Subcommand("info")
    @Description("Show info about Mimic and loaded services")
    fun info(sender: CommandSender) {
        val servicesManager = plugin.server.servicesManager
        val levelSystems = servicesManager.loadAll<BukkitLevelSystem.Provider>()
        val classSystems = servicesManager.loadAll<BukkitClassSystem.Provider>()
        val itemsRegistries = servicesManager.loadAll<BukkitItemsRegistry>()

        sender.send(
            "&2${plugin.description.fullName}",
            "&3Level Systems: &7${levelSystems.toMessage()}",
            "&3Class Systems: &7${classSystems.toMessage()}",
            "&3Items Registries: &7${itemsRegistries.toMessage()}"
        )
    }
}

private fun <ServiceT : MimicService> Collection<ServiceT>.toMessage(): String {
    return joinToString { service ->
        val color = if (service.isEnabled) "&a" else "&c"
        "$color${service.id}&7"
    }
}
