// Copyright (c) 2026 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.command

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text
import ru.endlesscode.mimic.resources.Resource
import ru.endlesscode.mimic.resources.ResourceState
import ru.endlesscode.mimic.resources.Resources

/**
 * Commands to deal with resources.
 * ```
 * /mimic resources (info) (player)
 * /mimic resources take [resource] [amount] (player)
 * /mimic resources give [resource] [amount] (player)
 * /mimic resources set [resource] [amount]|max (player)
 * ```
 */
internal fun CommandBuilder.resourcesSubcommand(mimic: Mimic) = literal("resources") {
    val resources = mimic.resources
    executesPlayer(infoCommandExecutor(mimic))

    // Show player's resources
    literal("info") {
        executesPlayer(infoCommandExecutor(mimic))
        argument(TARGET, ArgumentTypes.player()) {
            executesAny(infoCommandExecutor(mimic))
        }
    }

    // Take the amount, or nothing if the player has less
    changeSubcommand("take", resources) { player, resourceId, amount ->
        if (!resources.has(player, resourceId, amount)) {
            throw commandError("${player.name} has less than ${formatNumber(amount)} of '${resourceId.asString()}'.")
        }
        resources.take(player, resourceId, amount)
    }

    // Give the amount, up to the maximum
    changeSubcommand("give", resources, change = resources::give)

    // Set the amount, between 0 and the maximum
    changeSubcommand("set", resources, allowMax = true, change = resources::set)
}

/** Change of a resource of the player. Returns `false` if the resource wasn't changed. */
private typealias ResourceChange = (player: Player, resourceId: Key, amount: Double) -> Boolean

/**
 * Adds `/mimic resources [name] [resource] [amount] (player)` applying the [change].
 * With [allowMax], `max` can be used instead of the amount.
 */
private fun CommandBuilder.changeSubcommand(
    name: String,
    resources: Resources,
    allowMax: Boolean = false,
    change: ResourceChange,
) = literal(name) {
    argument(RESOURCE, KeyArgumentType("resource", "minecraft:food")) {
        suggestsIds({ context -> resourceHints(resources, context.source.sender) }) { resources.resourceIds() }
        argument(AMOUNT, DoubleArgumentType.doubleArg(0.0)) {
            executesChange(resources, change) { context, _ -> context.getArgument<Double>(AMOUNT) }
        }
        if (allowMax) literal(MAX) { executesChange(resources, change) { _, state -> state.max } }
    }
}

/** Amount for a [ResourceChange], taken from the command or from the current state of the resource. */
private typealias AmountSource = (context: CommandContext<CommandSourceStack>, state: ResourceState) -> Double

/** Adds the [change] execution for the sender and for the given player. */
private fun CommandBuilder.executesChange(resources: Resources, change: ResourceChange, amount: AmountSource) {
    executesPlayer(changeCommandExecutor(resources, change, amount))
    argument(TARGET, ArgumentTypes.player()) {
        executesAny(changeCommandExecutor(resources, change, amount))
    }
}

private fun Resources.resourceIds(): List<String> = getAllResources().map { it.key().asString() }

/** Suggests the primary resource of the sender and namespaces of resources. */
private fun resourceHints(resources: Resources, sender: CommandSender): List<SuggestionHint> {
    val primaryResource = (sender as? Player)?.let(resources::getPrimaryResource)
    val primaryHint = primaryResource?.let { SuggestionHint(it.key().asString(), Component.text("Your primary resource")) }
    return listOfNotNull(primaryHint) + namespaceHints(resources.resourceIds()) { "Resources from $it" }
}

private fun infoCommandExecutor(mimic: Mimic): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayerOrSender(TARGET, sender)
    val resources = mimic.resources
    val primaryResource = resources.getPrimaryResource(target)
    val otherResources = resources.getAllResources().filter { it != primaryResource }

    val message = text {
        appendHeader("Mimic Resources")
        appendProviderStat("Resource provider", primaryResource?.key()?.namespace() ?: "none")
        appendKey("${target.name}'s primary resource: ")
        if (primaryResource == null) appendValue("none") else appendResource(resources, target, primaryResource)
        appendLine()
        if (otherResources.isNotEmpty()) {
            appendKey("Other resources:")
            appendLine()
        }
        for ((namespace, namespaceResources) in otherResources.groupBy { it.key().namespace() }) {
            appendValue("  $namespace")
            appendLine()
            for (resource in namespaceResources) {
                append("    ")
                appendResource(resources, target, resource)
                appendLine()
            }
        }
        appendCommands(
            suggestedCommand("take", "/mimic resources take [resource] [amount] (player)", "Take the amount, or nothing if the player has less"),
            suggestedCommand("give", "/mimic resources give [resource] [amount] (player)", "Give the amount, up to the maximum"),
            suggestedCommand("set", "/mimic resources set [resource] [amount]|max (player)", "Set the amount, between 0 and the maximum"),
            suggestedCommand("info", "/mimic resources info (player)", "Show player's resources"),
        )
    }
    sender.sendMessage(message)
}

private fun changeCommandExecutor(
    resources: Resources,
    change: ResourceChange,
    amount: AmountSource,
): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayerOrSender(TARGET, sender)
    val resourceId = context.getArgument<Key>(RESOURCE)
    val state = resources.getState(target, resourceId)
        ?: throw commandError("${target.name} doesn't have resource '${resourceId.asString()}', or their data isn't loaded.")
    if (!change(target, resourceId, amount(context, state))) {
        throw commandError("Resource '${resourceId.asString()}' wasn't changed: the plugin doesn't support it or cancelled it.")
    }

    val newState = resources.getState(target, resourceId)
    sender.sendMessage(successText {
        append("New ${target.name}'s amount of ")
        appendId(resourceId.asString())
        append(": ")
        if (newState == null) append("unknown") else appendState(newState)
    })
}

/** Appends the resource name with its ID on hover, and the [player]'s amount of it. */
private fun TextComponent.Builder.appendResource(resources: Resources, player: Player, resource: Resource) {
    appendNamedId(resource.name(), resource.key().asString())
    append(" ")
    val state = resources.getState(player, resource.key())
    if (state == null) appendValue("none") else appendState(state)
}

/** Appends state like `15 / 20`, the maximum is secondary. */
private fun TextComponent.Builder.appendState(state: ResourceState) {
    appendValue(formatNumber(state.amount))
    append(" / ${formatNumber(state.max)}", SECONDARY_COLOR)
}

private const val TARGET = "target"
private const val RESOURCE = "resource"
private const val AMOUNT = "amount"
private const val MAX = "max"
