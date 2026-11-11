package ru.endlesscode.mimic.command

import com.mojang.brigadier.arguments.StringArgumentType
import org.bukkit.command.CommandSender
import ru.endlesscode.mimic.ExperimentalMimicApi
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.config.MimicConfig
import ru.endlesscode.mimic.impl.vanilla.MinecraftBlockRegistry
import ru.endlesscode.mimic.impl.vanilla.MinecraftItemsRegistry

/**
 * Commands to see and change Mimic config.
 *
 * ```
 * /mimic config
 * /mimic config [single-property] [value]
 * /mimic config [collection-property] add|remove [value]
 * /mimic config reset [property]
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
        stringSetArgument(VALUE, "level system", { mimic.getAllLevelSystemProviders().keys }) { sender, value ->
            config.levelSystem = value
            showConfig(sender)
        }
    }

    // Change the preferred class system
    literal("class-system") {
        stringSetArgument(VALUE, "class system", { mimic.getAllClassSystemProviders().keys }) { sender, value ->
            config.classSystem = value
            showConfig(sender)
        }
    }

    // Change the preferred inventory provider
    @OptIn(ExperimentalMimicApi::class)
    literal("inventory-provider") {
        stringSetArgument(VALUE, "inventory provider", { mimic.getAllPlayerInventoryProviders().keys }) { sender, value ->
            config.inventoryProvider = value
            showConfig(sender)
        }
    }

    // Enable/Disable an item registry in Mimic
    literal("disabled-items-registries") {
        literal(ACTION_ADD) {
            stringSetArgument(
                ITEMS_REGISTRY,
                "items registry",
                { mimic.getAllItemsRegistries().keys - MinecraftItemsRegistry.ID },
            ) { sender, itemsRegistry ->
                config.disabledItemsRegistries += itemsRegistry
                showConfig(sender)
            }
        }
        literal(ACTION_REMOVE) {
            stringSetArgument(ITEMS_REGISTRY, "disabled items registry", { config.disabledItemsRegistries }) { sender, itemsRegistry ->
                config.disabledItemsRegistries -= itemsRegistry
                showConfig(sender)
            }
        }
    }

    // Enable/Disable a block registry in Mimic
    @OptIn(ExperimentalMimicApi::class)
    literal("disabled-block-registries") {
        literal(ACTION_ADD) {
            stringSetArgument(
                BLOCK_REGISTRY,
                "block registry",
                { mimic.getAllBlockRegistries().keys - MinecraftBlockRegistry.ID },
            ) { sender, blockRegistry ->
                config.disabledBlockRegistries += blockRegistry
                showConfig(sender)
            }
        }
        literal(ACTION_REMOVE) {
            stringSetArgument(BLOCK_REGISTRY, "disabled block registry", { config.disabledBlockRegistries }) { sender, blockRegistry ->
                config.disabledBlockRegistries -= blockRegistry
                showConfig(sender)
            }
        }
    }

    // Reset a property to its default value
    literal(ACTION_RESET) {
        for (property in MimicConfig.properties) {
            literal(property.path) {
                executesAny { sender, _ ->
                    config.reset(property)
                    showConfig(sender)
                }
            }
        }
    }
}

private fun CommandBuilder.stringSetArgument(
    name: String,
    valueDescription: String,
    provideOptions: () -> Set<String>,
    action: (sender: CommandSender, value: String) -> Unit,
) = argument(name, StringArgumentType.word()) {
    suggestsStrings(provideOptions = provideOptions)
    executesAny { sender, context ->
        val value = context.getArgument<String>(name)
        if (value !in provideOptions()) throw commandError("Unknown $valueDescription '$value'")
        action(sender, value)
    }
}

private const val VALUE = "value"
private const val ITEMS_REGISTRY = "items-registry"
private const val BLOCK_REGISTRY = "block-registry"
private const val ACTION_ADD = "add"
private const val ACTION_REMOVE = "remove"
internal const val ACTION_RESET = "reset"
