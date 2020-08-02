package ru.endlesscode.mimic.bukkit

import org.bukkit.plugin.ServicePriority

internal fun registry.SubsystemPriority.toServicePriority(): ServicePriority {
    return when(this) {
        registry.SubsystemPriority.LOWEST -> ServicePriority.Lowest
        registry.SubsystemPriority.LOW -> ServicePriority.Low
        registry.SubsystemPriority.NORMAL -> ServicePriority.Normal
        registry.SubsystemPriority.HIGH -> ServicePriority.High
        registry.SubsystemPriority.HIGHEST -> ServicePriority.Highest
    }
}
