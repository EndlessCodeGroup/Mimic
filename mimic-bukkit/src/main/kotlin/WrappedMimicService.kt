package ru.endlesscode.mimic

internal abstract class WrappedMimicService(
    private val delegate: MimicService,
    pluginName: String,
) : MimicService {

    final override val id: String = serviceId(delegate.id, pluginName)

    override val isEnabled: Boolean
        get() = delegate.isEnabled

    private companion object {
        /**
         * Matches to Minecraft resource namespace regex.
         * https://minecraft.fandom.com/wiki/Resource_location#Java_Edition
         */
        val ID_REGEX by lazy { Regex("[a-z\\d._-]+") }

        fun serviceId(requestedId: String, pluginName: String): String {
            return requestedId.ifEmpty { pluginName }.lowercase().also { id ->
                require(id matches ID_REGEX) { "Invalid ID: $requestedId. It should match regex [a-z0-9._-]." }
            }
        }
    }
}
