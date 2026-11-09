@file:Suppress("UnstableApiUsage")

package ru.endlesscode.mimic.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

internal typealias CommandBuilder = ArgumentBuilder<CommandSourceStack, *>
internal typealias CommandAction<S> = (sender: S, context: CommandContext<CommandSourceStack>) -> Unit

internal fun CommandBuilder.literal(
    name: String,
    configure: LiteralArgumentBuilder<CommandSourceStack>.() -> Unit,
) {
    then(Commands.literal(name).apply(configure))
}

internal fun <T : Any> CommandBuilder.argument(
    name: String,
    type: ArgumentType<T>,
    configure: RequiredArgumentBuilder<CommandSourceStack, T>.() -> Unit,
) {
    then(Commands.argument(name, type).apply(configure))
}

internal fun CommandBuilder.executesAny(action: CommandAction<CommandSender>) {
    executes { context ->
        action(context.source.sender, context)
        Command.SINGLE_SUCCESS
    }
}

internal fun CommandBuilder.executesPlayer(action: CommandAction<Player>) {
    executes { context ->
        val player = context.source.sender as? Player ?: throw commandError("This command can be used only by players")
        action(player, context)
        Command.SINGLE_SUCCESS
    }
}

/** Suggestion with an optional [tooltip] shown when hovering over it. */
internal class SuggestionHint(val text: String, val tooltip: Component? = null)

internal fun SuggestionsBuilder.suggest(hint: SuggestionHint): SuggestionsBuilder =
    if (hint.tooltip == null) {
        suggest(hint.text)
    } else {
        suggest(hint.text, MessageComponentSerializer.message().serialize(hint.tooltip))
    }

/**
 * Suggests options containing the already typed input, ignoring case.
 * Brigadier sorts suggestions alphabetically, so the order of options doesn't matter.
 */
internal fun RequiredArgumentBuilder<CommandSourceStack, *>.suggestsStrings(provideOptions: () -> Collection<String>) {
    suggests { _, builder ->
        val input = builder.remainingLowerCase
        provideOptions()
            .filter { input in it.lowercase() }
            .forEach(builder::suggest)
        builder.buildFuture()
    }
}

/**
 * Suggests namespaced IDs like `minecraft:stone`, matching the typed input against the part after namespace.
 * - Empty input: hints from [provideEmptyInputHints].
 * - `namespace:name`: IDs from the namespace containing `name`, or similar namespaces if the namespace is unknown.
 *   Namespace `mimic` searches over all namespaces.
 * - `name` without namespace: IDs from all namespaces containing `name` and namespaces starting with `name`.
 */
internal fun RequiredArgumentBuilder<CommandSourceStack, *>.suggestsIds(
    provideEmptyInputHints: (CommandSender) -> List<SuggestionHint>,
    provideIds: () -> Collection<String>,
) {
    suggests { context, builder ->
        val input = builder.remainingLowerCase
        val hints = if (input.isEmpty()) {
            provideEmptyInputHints(context.source.sender)
        } else {
            idHints(input, provideIds())
        }
        hints.distinctBy { it.text }.forEach { builder.suggest(it) }
        builder.buildFuture()
    }
}

private fun idHints(input: String, ids: Collection<String>): List<SuggestionHint> {
    val parsedIds = ids.map { id -> ParsedId(id) }
    val namespaces = parsedIds.map { it.namespace }.toSet()

    fun idsContaining(name: String, namespace: String? = null) = parsedIds
        .filter { (namespace == null || it.namespace == namespace) && name in it.name }
        .map { SuggestionHint(it.id) }

    if (':' !in input) {
        return idsContaining(input) + namespaces
            .filter { it.startsWith(input) }
            .map { SuggestionHint("$it:", Component.text("Namespace")) }
    }

    val namespace = input.substringBefore(':')
    val name = input.substringAfter(':')
    return when (namespace) {
        SEARCH_ALL_NAMESPACE -> idsContaining(name)
        in namespaces -> idsContaining(name, namespace)
        else -> namespaces
            .filter { namespace in it }
            .map { SuggestionHint("$it:$name", Component.text("Did you mean $it?")) }
    }
}

/** ID split to the lowercase namespace and name. */
private class ParsedId(val id: String) {
    val namespace: String = id.substringBefore(':', missingDelimiterValue = "").lowercase()
    val name: String = id.substringAfter(':').lowercase()
}

/** Namespace used by Mimic registries to search IDs over all registries. */
private const val SEARCH_ALL_NAMESPACE = "mimic"

internal fun CommandContext<*>.hasArgument(name: String): Boolean = nodes.any { it.node.name == name }

internal inline fun <reified T : Any> CommandContext<*>.getArgument(name: String): T =
    getArgument(name, T::class.java)

/** Returns the value of an optional argument, or `null` if it wasn't specified. */
internal inline fun <reified T : Any> CommandContext<*>.getArgumentOrNull(name: String): T? =
    if (hasArgument(name)) getArgument(name) else null

internal fun CommandContext<CommandSourceStack>.getPlayer(name: String): Player =
    getArgument<PlayerSelectorArgumentResolver>(name).resolve(source).firstOrNull()
        ?: throw commandError("No player was found")

/** Returns the player from the argument [name]. If it is not specified, [sender] must be a player. */
internal fun CommandContext<CommandSourceStack>.getPlayerOrSender(name: String, sender: CommandSender): Player =
    if (hasArgument(name)) getPlayer(name) else sender as Player

internal fun CommandContext<CommandSourceStack>.getBlockLocation(name: String): Location =
    getArgument<BlockPositionResolver>(name).resolve(source).toLocation(source.location.world)

internal fun commandError(message: String): CommandSyntaxException =
    SimpleCommandExceptionType(LiteralMessage(message)).create()

/**
 * Resource location like `minecraft:stone`, shown to the client as a key.
 * Unlike [ArgumentTypes.key], keeps an ID without namespace as is instead of adding the `minecraft` namespace.
 */
internal object ResourceLocationArgumentType : CustomArgumentType<String, Key> {

    private val expectedId = SimpleCommandExceptionType(LiteralMessage("Expected ID"))

    override fun parse(reader: StringReader): String {
        val start = reader.cursor
        while (reader.canRead() && reader.peek().isAllowedInResourceLocation()) reader.skip()
        if (reader.cursor == start) throw expectedId.createWithContext(reader)
        return reader.string.substring(start, reader.cursor)
    }

    override fun getNativeType(): ArgumentType<Key> = ArgumentTypes.key()

    private fun Char.isAllowedInResourceLocation() = this in 'a'..'z' || this in '0'..'9' || this in "_-.:/"
}
