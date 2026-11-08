package ru.endlesscode.mimic.command

import com.mojang.brigadier.arguments.StringArgumentType
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
internal fun CommandBuilder.configSubcommand(
    mimic: Mimic,
    config: MimicConfig,
) = literal("config") {
    val showConfig = { sender: CommandSender ->
        val message = buildConfigMessage(mimic, config)
        sender.sendMessage(message)
    }

    // Show Mimic config
    executesAny { sender, _ -> showConfig(sender) }

    // Change the preferred level system
    literal("level-system") {
        stringSetArgument(VALUE, { mimic.getAllLevelSystemProviders().keys }) { sender, value ->
            config.levelSystem = value
            showConfig(sender)
        }
    }

    // Change the preferred class system
    literal("class-system") {
        stringSetArgument(VALUE, { mimic.getAllClassSystemProviders().keys }) { sender, value ->
            config.classSystem = value
            showConfig(sender)
        }
    }

    // Change the preferred inventory provider
    @OptIn(ExperimentalMimicApi::class)
    literal("inventory-provider") {
        stringSetArgument(VALUE, { mimic.getAllPlayerInventoryProviders().keys }) { sender, value ->
            config.inventoryProvider = value
            showConfig(sender)
        }
    }

    // Enable/Disable an item registry in Mimic
    literal("disabled-items-registries") {
        for (action in listOf(ACTION_ADD, ACTION_REMOVE)) {
            literal(action) {
                stringSetArgument(
                    ITEMS_REGISTRY,
                    { mimic.getAllItemsRegistries().keys - MimicConfig.DEFAULT_ITEMS_REGISTRIES },
                ) { sender, itemsRegistry ->
                    if (action == ACTION_ADD) {
                        config.disabledItemsRegistries += itemsRegistry
                    } else {
                        config.disabledItemsRegistries -= itemsRegistry
                    }
                    showConfig(sender)
                }
            }
        }
    }
}

private fun CommandBuilder.stringSetArgument(
    name: String,
    provideOptions: () -> Set<String>,
    action: (sender: CommandSender, value: String) -> Unit,
) = argument(name, StringArgumentType.word()) {
    suggestsStrings(provideOptions)
    executesAny { sender, context -> action(sender, StringArgumentType.getString(context, name)) }
}

private const val VALUE = "value"
private const val ITEMS_REGISTRY = "items-registry"
private const val ACTION_ADD = "add"
private const val ACTION_REMOVE = "remove"
