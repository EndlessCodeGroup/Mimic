package ru.endlesscode.mimic.internal

/**
 * Mimic ID is a [resource location](https://minecraft.wiki/w/Resource_location),
 * where the namespace is the ID of a registry, for example `craftengine:default/palm_log`.
 *
 * [namespace] is `null` if the ID may belong to any registry.
 */
internal data class MimicId(val namespace: String?, val path: String) {

    /** Legacy IDs contain `:` in the [path], for example `ia:itemsadder:ruby_sword`. */
    val isLegacy: Boolean get() = ':' in path

    /** Returns `true` if this ID may belong to the registry with the given [registryId]. */
    fun matches(registryId: String): Boolean = namespace == null || namespace == registryId
}

/** Namespace meaning that an ID may belong to any registry. */
private const val ANY_NAMESPACE = "mimic"

private val namespaceRegex = Regex("[a-z0-9._-]+")
private val pathRegex = Regex("[a-z0-9._/-]+")
private val legacyPathRegex = Regex("[a-z0-9._/:-]+")

/**
 * Parses the given [id], or returns `null` if the ID is invalid.
 * ID without namespace or with the `mimic` namespace may belong to any registry.
 *
 * If [allowLegacy] is `true`, the path may contain `:`.
 */
internal fun parseMimicId(id: String, allowLegacy: Boolean = false): MimicId? {
    val separatorIndex = id.indexOf(':')
    val namespace = if (separatorIndex == -1) null else id.substring(0, separatorIndex)
    val path = id.substring(separatorIndex + 1)

    if (namespace != null && !namespace.matches(namespaceRegex)) return null
    if (!path.matches(if (allowLegacy) legacyPathRegex else pathRegex)) return null
    return MimicId(namespace.takeUnless { it == ANY_NAMESPACE }, path)
}
