// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

@file:Suppress("UnstableApiUsage")

package ru.endlesscode.mimic

import org.bstats.bukkit.Metrics
import org.bstats.charts.DrilldownPie
import org.bstats.charts.SimplePie
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

// https://bstats.org/plugin/bukkit/Mimic/8413
private const val BSTATS_PLUGIN_ID = 8413

/** Reported when there is nothing else to report, to count such servers too. */
private val NONE_CHART_DATA = mapOf("none" to mapOf("none" to 1))

/** Starts sending bStats metrics of the [mimic] services. */
@OptIn(ExperimentalMimicApi::class)
internal fun Plugin.initMetrics(mimic: MimicImpl) {
    val metrics = Metrics(this, BSTATS_PLUGIN_ID)

    metrics.addCustomChart(DrilldownPie("level_providers") { registryChartData(mimic.levelProviders) })
    metrics.addCustomChart(DrilldownPie("class_providers") { registryChartData(mimic.classProviders) })
    metrics.addCustomChart(DrilldownPie("resource_providers") { registryChartData(mimic.resourceProviders) })
    metrics.addCustomChart(DrilldownPie("items_registries") { registryChartData(mimic.itemsRegistries) })
    metrics.addCustomChart(DrilldownPie("block_providers") { registryChartData(mimic.blockProviders) })
    metrics.addCustomChart(SimplePie("inventory_provider") { mimic.getPlayerInventoryProvider().id })
    metrics.addCustomChart(DrilldownPie("dependent_plugins") { dependentPluginsChartData() })
}

/**
 * Maps IDs of enabled services, except the default one, to the name and version of the plugin behind each.
 * Integrations built into Mimic and services registered by other plugins via a deprecated API are marked.
 */
private fun Plugin.registryChartData(registry: ServiceRegistry<*>): Map<String, Map<String, Int>> {
    val mimicPlugin = this
    return registry.enabledEntries
        .filterNot { it.service.id == registry.defaultId }
        .associate { entry ->
            val isBuiltIn = entry.plugin == mimicPlugin
            // IDs of built-in integrations match names of the integrated plugins
            val plugin = server.pluginManager.plugins
                .takeIf { isBuiltIn }
                ?.find { it.name.equals(entry.service.id, ignoreCase = true) }
                ?: entry.plugin
            val pluginMeta = plugin.pluginMeta
            val mark = when {
                isBuiltIn -> " (built-in)"
                entry.isBridge -> " (deprecated API)"
                else -> ""
            }
            entry.service.id to mapOf("${pluginMeta.name} ${pluginMeta.version}$mark" to 1)
        }
        .ifEmpty { NONE_CHART_DATA }
}

/** Maps names of plugins declaring a dependency on this plugin to their versions. */
private fun Plugin.dependentPluginsChartData(): Map<String, Map<String, Int>> {
    val mimicName = pluginMeta.name
    return server.pluginManager.plugins
        .map { it.pluginMeta }
        .filter { mimicName in it.pluginDependencies || mimicName in it.pluginSoftDependencies }
        .associate { it.name to mapOf(it.version to 1) }
        .ifEmpty { NONE_CHART_DATA }
}
