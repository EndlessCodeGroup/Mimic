package ru.endlesscode.mimic.config

import org.bukkit.configuration.Configuration
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun <T : Any> Configuration.property(configProperty: ConfigProperty<T>): ReadWriteProperty<MimicConfig, T> {
    return object : ReadWriteProperty<MimicConfig, T> {
        override fun getValue(thisRef: MimicConfig, property: KProperty<*>): T = get(configProperty)

        override fun setValue(thisRef: MimicConfig, property: KProperty<*>, value: T) {
            val valueChanged = get(configProperty) != value
            if (valueChanged) {
                set(configProperty, value)
                thisRef.save()
            }
        }
    }
}
