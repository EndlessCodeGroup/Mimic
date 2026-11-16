// Copyright (c) 2022 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.config

import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.impl.vanilla.MinecraftBlockProvider
import ru.endlesscode.mimic.impl.vanilla.MinecraftItemProvider
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.internal.applyDefaults
import ru.endlesscode.mimic.internal.setComments
import ru.endlesscode.mimic.internal.setHeader
import java.io.File

internal class MimicConfig(
    pluginName: String,
    private val file: File,
    private val configuration: FileConfiguration = YamlConfiguration(),
) {

    var levelProvider: String by configuration.property(LEVEL_PROVIDER)
    var classProvider: String by configuration.property(CLASS_PROVIDER)
    var resourceProvider: String by configuration.property(RESOURCE_PROVIDER)
    var inventoryProvider: String by configuration.property(INVENTORY_PROVIDER)
    var disabledItemProviders: Set<String> by configuration.property(DISABLED_ITEM_PROVIDERS)
    var disabledBlockProviders: Set<String> by configuration.property(DISABLED_BLOCK_PROVIDERS)

    constructor(plugin: Plugin) : this(plugin.toString(), File(plugin.dataFolder, "config.yml"))

    init {
        with(configuration) {
            options().setHeader(
                pluginName,
                "",
                "Here you can configure Mimic APIs.",
                "Use command '/mimic' to list available implementations.",
            )
            properties.forEach(::addDefault)
        }
        reload()
    }

    fun reload() {
        try {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            configuration.load(file)
            migrateOption(LEGACY_LEVEL_SYSTEM_PATH, LEVEL_PROVIDER)
            migrateOption(LEGACY_CLASS_SYSTEM_PATH, CLASS_PROVIDER)
            migrateOption(LEGACY_DISABLED_ITEMS_REGISTRIES_PATH, DISABLED_ITEM_PROVIDERS)
            migrateOption(LEGACY_DISABLED_BLOCK_REGISTRIES_PATH, DISABLED_BLOCK_PROVIDERS)
            validateConfigValues()
        } catch (e: Throwable) {
            Log.w(e, "Failed to load config.")
        }
        save()
    }

    /** Moves the value of the option with [legacyPath] to the [property], if the latter isn't set. */
    private fun migrateOption(legacyPath: String, property: ConfigProperty<*>) {
        val value = configuration.get(legacyPath) ?: return
        if (!configuration.isSet(property.path)) configuration.set(property.path, value)
        configuration.set(legacyPath, null)
        Log.i("Config: Option '$legacyPath' is renamed to '${property.path}'.")
    }

    private fun validateConfigValues() {
        val configuredDisabledItemProviders = disabledItemProviders
        disabledItemProviders = configuredDisabledItemProviders - MinecraftItemProvider.ID
        val notDisabledItemProviders = configuredDisabledItemProviders - disabledItemProviders
        if (notDisabledItemProviders.isNotEmpty()) {
            Log.w("Config: Item providers $notDisabledItemProviders can not be disabled. It will be removed from config.")
        }

        val configuredDisabledBlockProviders = disabledBlockProviders
        disabledBlockProviders = configuredDisabledBlockProviders - MinecraftBlockProvider.ID
        val notDisabledBlockProviders = configuredDisabledBlockProviders - disabledBlockProviders
        if (notDisabledBlockProviders.isNotEmpty()) {
            Log.w("Config: Block providers $notDisabledBlockProviders can not be disabled. It will be removed from config.")
        }
    }

    /** Resets the given [property] to its default value. */
    fun reset(property: ConfigProperty<*>) {
        configuration.set(property.path, property.defaultValue)
        save()
    }

    fun save() {
        try {
            configuration.applyDefaults()
            configuration.addComments()
            configuration.save(file)
        } catch (e: Throwable) {
            Log.w(e, "Failed to save config.")
        }
    }

    private fun Configuration.addComments() {
        properties.forEachIndexed { index, property ->
            val lineBreak: Array<String?> = if (index != 0) arrayOf(null) else emptyArray()
            setComments(
                path = property.path,
                *lineBreak,
                property.formattedTitle,
                *property.comments,
            )
        }
    }

    internal companion object {
        private const val EMPTY_VALUE_MESSAGE =
            "If the value is empty, will be used implementation with the highest priority."

        private const val LEGACY_LEVEL_SYSTEM_PATH = "level-system"
        private const val LEGACY_CLASS_SYSTEM_PATH = "class-system"
        private const val LEGACY_DISABLED_ITEMS_REGISTRIES_PATH = "disabled-items-registries"
        private const val LEGACY_DISABLED_BLOCK_REGISTRIES_PATH = "disabled-block-registries"

        val LEVEL_PROVIDER = StringConfigProperty(
            path = "level-provider",
            title = "Levels API",
            comments = arrayOf(
                "Specify here the plugin providing the player's level.",
                "Its primary track is used as the player's level.",
                "If the value is empty or the plugin isn't installed, Mimic asks plugins one by one,",
                "from the highest priority, and uses the first one that has the player's level.",
            )
        )
        val CLASS_PROVIDER = StringConfigProperty(
            path = "class-provider",
            title = "Classes API",
            comments = arrayOf(
                "Specify here the plugin providing the player's class.",
                "The class in its primary slot is used as the player's class.",
                "If the value is empty or the plugin isn't installed, Mimic asks plugins one by one,",
                "from the highest priority, and uses the first one that has the player's class.",
            ),
        )
        val RESOURCE_PROVIDER = StringConfigProperty(
            path = "resource-provider",
            title = "Resources API",
            comments = arrayOf(
                "Specify here the plugin providing the player's mana.",
                "Its primary resource is used as the player's mana.",
                "If the value is empty or the plugin isn't installed, Mimic asks plugins one by one,",
                "from the highest priority, and uses the first one that has the player's mana.",
            ),
        )
        val INVENTORY_PROVIDER = StringConfigProperty(
            path = "inventory-provider",
            title = "PlayerInventory API",
            comments = arrayOf(
                "Specify here the preferred player inventory provider implementation.",
                EMPTY_VALUE_MESSAGE,
            ),
        )
        val DISABLED_ITEM_PROVIDERS = StringSetConfigProperty(
            path = "disabled-item-providers",
            title = "Items API",
            comments = arrayOf(
                "List here plugins whose items should not be available via Mimic.",
                "You can't disable 'minecraft' item provider.",
                "By default, all item providers are enabled.",
            ),
        )

        val DISABLED_BLOCK_PROVIDERS = StringSetConfigProperty(
            path = "disabled-block-providers",
            title = "Blocks API",
            comments = arrayOf(
                "List here plugins whose blocks should not be available via Mimic.",
                "You can't disable 'minecraft' block provider.",
                "By default, all block providers are enabled.",
            ),
        )

        val properties = listOf(
            LEVEL_PROVIDER,
            CLASS_PROVIDER,
            RESOURCE_PROVIDER,
            INVENTORY_PROVIDER,
            DISABLED_ITEM_PROVIDERS,
            DISABLED_BLOCK_PROVIDERS,
        )
    }
}
