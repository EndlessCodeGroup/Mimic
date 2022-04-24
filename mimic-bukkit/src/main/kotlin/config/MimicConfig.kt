package ru.endlesscode.mimic.config

import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
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

    var levelSystem: String by configuration.property(LEVEL_SYSTEM)
    var classSystem: String by configuration.property(CLASS_SYSTEM)
    var inventoryProvider: String by configuration.property(INVENTORY_PROVIDER)
    var disabledItemsRegistries: Set<String> by configuration.property(DISABLED_ITEMS_REGISTRIES)

    constructor(plugin: Plugin) : this(plugin.toString(), File(plugin.dataFolder, "config.yml"))

    init {
        with(configuration) {
            options().setHeader(
                pluginName,
                "",
                "Here you can configure Mimic APIs.",
                "Use command '/mimic info' to list available implementations.",
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
            validateConfigValues()
        } catch (e: Throwable) {
            Log.w(e, "Failed to load config.")
        }
        save()
    }

    private fun validateConfigValues() {
        val configuredDisabledItemsRegistries = disabledItemsRegistries
        disabledItemsRegistries = configuredDisabledItemsRegistries - DEFAULT_ITEMS_REGISTRIES

        val notDisabledRegistries = configuredDisabledItemsRegistries - disabledItemsRegistries
        if (notDisabledRegistries.isNotEmpty()) {
            Log.w("Config: Items registries $notDisabledRegistries can not be disabled. It will be removed from config.")
        }
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
                "[${property.title}]",
                *property.comments,
            )
        }
    }

    internal companion object {
        private const val EMPTY_VALUE_MESSAGE =
            "If the value is empty, will be used implementation with the highest priority."

        val DEFAULT_ITEMS_REGISTRIES = setOf("mimic", "minecraft")

        val LEVEL_SYSTEM = StringConfigProperty(
            path = "level-system",
            title = "LevelSystem API",
            comments = arrayOf(
                "Specify here the preferred level system implementation.",
                EMPTY_VALUE_MESSAGE,
            )
        )
        val CLASS_SYSTEM = StringConfigProperty(
            path = "class-system",
            title = "ClassSystem API",
            comments = arrayOf(
                "Specify here the preferred class system implementation.",
                EMPTY_VALUE_MESSAGE,
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
        val DISABLED_ITEMS_REGISTRIES = StringSetConfigProperty(
            path = "disabled-items-registries",
            title = "ItemsRegistry API",
            comments = arrayOf(
                "List here disabled items registry implementations.",
                "You can't disable 'mimic' and 'minecraft' items registries.",
                "By default, all implementations are enabled.",
            ),
        )

        private val properties = listOf(
            LEVEL_SYSTEM,
            CLASS_SYSTEM,
            INVENTORY_PROVIDER,
            DISABLED_ITEMS_REGISTRIES,
        )
    }
}
