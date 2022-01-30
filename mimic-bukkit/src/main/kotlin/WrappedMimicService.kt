package ru.endlesscode.mimic

import org.bukkit.plugin.PluginManager

internal abstract class WrappedMimicService(
    private val pluginName: String,
    private val pluginManager: PluginManager,
) : MimicService {

    protected abstract val delegate: MimicService

    @Suppress("LeakingThis")
    final override val id: String = serviceId(delegate.id, pluginName)

    final override val isEnabled: Boolean
        get() = pluginManager.isPluginEnabled(pluginName) || delegate.isEnabled

    private companion object {
        val ID_REGEX by lazy { Regex("[a-z0-9_]+") }

        fun serviceId(requestedId: String, pluginName: String): String {
            return requestedId.ifEmpty { pluginName }.lowercase().also { id ->
                require(id matches ID_REGEX) {
                    """
                    Invalid ID: $requestedId
                    It should contain only lowercase Latin letters and digits (a-z, 0-9).
                    """.trimIndent()
                }
            }
        }
    }
}
