package ru.endlesscode.mimic.bukkit

import org.bukkit.plugin.ServicePriority
import ru.endlesscode.mimic.api.system.registry.SubsystemPriority

internal fun SubsystemPriority.toServicePriority(): ServicePriority {
    return when(this) {
        SubsystemPriority.LOWEST -> ServicePriority.Lowest
        SubsystemPriority.LOW -> ServicePriority.Low
        SubsystemPriority.NORMAL -> ServicePriority.Normal
        SubsystemPriority.HIGH -> ServicePriority.High
        SubsystemPriority.HIGHEST -> ServicePriority.Highest
    }
}
