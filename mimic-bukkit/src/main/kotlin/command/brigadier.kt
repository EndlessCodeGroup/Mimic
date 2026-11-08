package ru.endlesscode.mimic.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
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

/** Suggests options starting with the already typed input, ignoring case. */
internal fun RequiredArgumentBuilder<CommandSourceStack, *>.suggestsStrings(provideOptions: () -> Collection<String>) {
    suggests { _, builder ->
        val input = builder.remainingLowerCase
        provideOptions()
            .filter { it.lowercase().startsWith(input) }
            .forEach(builder::suggest)
        builder.buildFuture()
    }
}

internal fun CommandContext<*>.hasArgument(name: String): Boolean = nodes.any { it.node.name == name }

internal fun CommandContext<CommandSourceStack>.getPlayer(name: String): Player =
    getArgument(name, PlayerSelectorArgumentResolver::class.java).resolve(source).firstOrNull()
        ?: throw commandError("No player was found")

internal fun CommandContext<CommandSourceStack>.getPlayerOrDefault(name: String, default: Player): Player =
    if (hasArgument(name)) getPlayer(name) else default

internal fun CommandContext<CommandSourceStack>.getBlockLocation(name: String): Location =
    getArgument(name, BlockPositionResolver::class.java).resolve(source).toLocation(source.location.world)

internal fun commandError(message: String): CommandSyntaxException =
    SimpleCommandExceptionType(LiteralMessage(message)).create()
