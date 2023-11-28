package ru.endlesscode.mimic.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.multiLiteralArgument
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.command.CommandSender
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.config.MimicConfig

/**
 * Commands to see and change Mimic config.
 *
 * ```
 * /mimic config
 * /mimic config [single-property] [value]
 * /mimic config [collection-property] add|remove [value]
 * ```
 */
internal fun CommandAPICommand.configSubcommand(
    mimic: Mimic,
    config: MimicConfig,
    audiences: BukkitAudiences,
) = subcommand("config") {
    val showConfig = { sender: CommandSender ->
        val message = buildConfigMessage(mimic, config)
        audiences.sender(sender).sendMessage(message)
    }

    withShortDescription("Show Mimic config")
    anyExecutor { sender, _ -> showConfig(sender) }

    subcommand("level-system") {
        withShortDescription("Change the preferred level system")
        stringSetArgument(VALUE) { mimic.getAllLevelSystemProviders().keys }
        anyExecutor { sender, args ->
            config.levelSystem = args[VALUE] as String
            showConfig(sender)
        }
    }

    subcommand("class-system") {
        withShortDescription("Change the preferred class system")
        stringSetArgument(VALUE) { mimic.getAllClassSystemProviders().keys }
        anyExecutor { sender, args ->
            config.classSystem = args[VALUE] as String
            showConfig(sender)
        }
    }

    @OptIn(ExperimentalMimicApi::class)
    subcommand("inventory-provider") {
        withShortDescription("Change the preferred inventory provider")
        stringSetArgument(VALUE) { mimic.getAllPlayerInventoryProviders().keys }
        anyExecutor { sender, args ->
            config.inventoryProvider = args[VALUE] as String
            showConfig(sender)
        }
    }

    subcommand("disabled-items-registries") {
        withShortDescription("Enable/Disable an item registry in Mimic")
        multiLiteralArgument(nodeName = ACTION, ACTION_ADD, ACTION_REMOVE)
        stringSetArgument(ITEMS_REGISTRY) {
            mimic.getAllItemsRegistries().keys - MimicConfig.DEFAULT_ITEMS_REGISTRIES
        }

        anyExecutor { sender, args ->
            val action = args[ACTION] as String
            val itemsRegistry = args[ITEMS_REGISTRY] as String
            if (action == ACTION_ADD) {
                config.disabledItemsRegistries += itemsRegistry
            } else {
                config.disabledItemsRegistries -= itemsRegistry
            }
            showConfig(sender)
        }
    }
}

private fun CommandAPICommand.stringSetArgument(
    nodeName: String,
    provideOptions: () -> Set<String>
) = stringArgument(nodeName) {
    replaceSuggestions(ArgumentSuggestions.stringCollection { provideOptions() })
}

private const val VALUE = "value"
private const val ITEMS_REGISTRY = "items-registry"
private const val ACTION = "action"
private const val ACTION_ADD = "add"
private const val ACTION_REMOVE = "remove"
