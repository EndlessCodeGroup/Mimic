package ru.endlesscode.mimic.command

import co.aikar.commands.AbstractCommandManager
import co.aikar.commands.MimicCommand
import co.aikar.commands.annotation.*
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.command.CommandSender
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.config.MimicConfig

@OptIn(ExperimentalMimicApi::class)
@CommandAlias("%command")
@CommandPermission("%perm")
@Subcommand("config")
internal class ConfigCommand(
    private val mimic: Mimic,
    private val config: MimicConfig,
    private val audiences: BukkitAudiences,
) : MimicCommand() {

    override fun afterRegister(manager: AbstractCommandManager) {
        manager.commandCompletions.registerAsyncCompletion("level-system") {
            mimic.getAllLevelSystemProviders().keys
        }
        manager.commandCompletions.registerAsyncCompletion("class-system") {
            mimic.getAllClassSystemProviders().keys
        }
        manager.commandCompletions.registerAsyncCompletion("inventory-provider") {
            mimic.getAllPlayerInventoryProviders().keys
        }
        manager.commandCompletions.registerAsyncCompletion("items-registry") {
            mimic.getAllItemsRegistries().keys - MimicConfig.DEFAULT_ITEMS_REGISTRIES
        }
    }

    @Default
    @Description("Show config")
    fun showConfig(sender: CommandSender) {
        val message = buildConfigMessage(mimic, config)
        audiences.sender(sender).sendMessage(message)
    }

    @Subcommand("level-system")
    @CommandCompletion("@level-system")
    fun setLevelSystem(sender: CommandSender, levelSystem: String) {
        config.levelSystem = levelSystem
        showConfig(sender)
    }

    @Subcommand("class-system")
    @CommandCompletion("@class-system")
    fun setClassSystem(sender: CommandSender, classSystem: String) {
        config.classSystem = classSystem
        showConfig(sender)
    }

    @Subcommand("inventory-provider")
    @CommandCompletion("@inventory-provider")
    fun setInventoryProvider(sender: CommandSender, inventoryProvider: String) {
        config.inventoryProvider = inventoryProvider
        showConfig(sender)
    }

    @Subcommand("disabled-items-registries add")
    @CommandCompletion("@items-registry")
    fun disableItemsRegistry(sender: CommandSender, inventoryProvider: String) {
        config.disabledItemsRegistries += inventoryProvider
        showConfig(sender)
    }

    @Subcommand("disabled-items-registries remove")
    @CommandCompletion("@items-registry")
    fun enableItemsRegistry(sender: CommandSender, inventoryProvider: String) {
        config.disabledItemsRegistries -= inventoryProvider
        showConfig(sender)
    }
}
