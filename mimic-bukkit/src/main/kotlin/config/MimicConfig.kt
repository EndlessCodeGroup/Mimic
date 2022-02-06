package ru.endlesscode.mimic.config

import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.internal.Log
import ru.endlesscode.mimic.internal.applyDefaults
import ru.endlesscode.mimic.internal.setComments
import java.io.File

internal class MimicConfig(
    pluginName: String,
    private val file: File,
    private val configuration: FileConfiguration = YamlConfiguration(),
) {

    var levelSystem: String = ""
        private set
    var classSystem: String = ""
        private set
    var disabledItemsRegistries: Set<String> = emptySet()
        private set

    constructor(plugin: Plugin) : this(plugin.toString(), File(plugin.dataFolder, "config.yml"))

    init {
        with(configuration) {
            options().setHeader(listOf(
                pluginName,
                "",
                "Here you can configure Mimic APIs.",
                "Guide: https://github.com/EndlessCodeGroup/Mimic",
                "",
                "Use command '/mimic info' to list available implementations."
            ))
            addDefault(LEVEL_SYSTEM, "")
            addDefault(CLASS_SYSTEM, "")
            addDefault(DISABLED_ITEMS_REGISTRIES, emptyList<String>())
        }
        reload()
    }

    fun reload() {
        try {
            configuration.load(file)
            readConfigValues()
        } catch (e: Exception) {
            Log.w(e, "Failed to load config.")
        }
        save()
    }

    private fun readConfigValues() {
        levelSystem = configuration.getString(LEVEL_SYSTEM).orEmpty()
        classSystem = configuration.getString(CLASS_SYSTEM).orEmpty()

        val disabledItemsRegistries = configuration.getStringList(DISABLED_ITEMS_REGISTRIES).toSet()
        this.disabledItemsRegistries = disabledItemsRegistries - DEFAULT_ITEMS_REGISTRIES

        val notDisabledRegistries = disabledItemsRegistries - this.disabledItemsRegistries
        if (notDisabledRegistries.isNotEmpty()) {
            Log.w("Config: Items registries $notDisabledRegistries can not be disabled.")
        }
    }

    private fun save() {
        try {
            configuration.applyDefaults()
            configuration.addComments()
            configuration.save(file)
        } catch (e: Exception) {
            Log.w(e, "Failed to save config.")
        }
    }

    private fun Configuration.addComments() {
        setComments(
            path = LEVEL_SYSTEM,
            "[LevelSystem API]",
            "Specify here the preferred level system implementation.",
            "If the value is empty, will be used implementation with the highest priority.",
        )
        setComments(
            path = CLASS_SYSTEM,
            "[ClassSystem API]",
            "Specify here the preferred class system implementation.",
            "If the value is empty, will be used implementation with the highest priority.",
        )
        setComments(
            path = DISABLED_ITEMS_REGISTRIES,
            "[ItemsRegistry API]",
            "List here disabled items registry implementations.",
            "You can't disable 'mimic' and 'minecraft' items registries.",
            "By default, all implementations are enabled.",
        )
    }

    private companion object {
        const val LEVEL_SYSTEM = "level-system"
        const val CLASS_SYSTEM = "class-system"
        const val DISABLED_ITEMS_REGISTRIES = "disabled-items-registries"

        val DEFAULT_ITEMS_REGISTRIES = setOf("mimic", "minecraft")
    }
}
