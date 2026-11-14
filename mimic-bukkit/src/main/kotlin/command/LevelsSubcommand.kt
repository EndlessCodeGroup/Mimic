// Copyright (c) 2019 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.command

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.impl.mimic.getPrimaryLevelProviderId
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text
import ru.endlesscode.mimic.level.LevelProgress
import ru.endlesscode.mimic.level.Levels

/**
 * Commands to deal with levels.
 * ```
 * /mimic levels (info) (player)
 * /mimic levels set [track] [level] (player)
 * /mimic levels give [track] [exp] (player)
 * /mimic levels take [track] [exp] (player)
 * ```
 */
internal fun CommandBuilder.levelsSubcommand(mimic: Mimic) = literal("levels") {
    val levels = mimic.levels
    executesPlayer(infoCommandExecutor(mimic))

    // Show player's tracks
    literal("info") {
        executesPlayer(infoCommandExecutor(mimic))
        argument(TARGET, ArgumentTypes.player()) {
            executesAny(infoCommandExecutor(mimic))
        }
    }

    // Set level, resetting exp within the level
    changeSubcommand("set", levels, LEVEL, IntegerArgumentType.integer(0)) { player, trackId, context ->
        levels.setLevel(player, trackId, context.getArgument<Int>(LEVEL))
    }

    // Give exp
    changeSubcommand("give", levels, EXP, DoubleArgumentType.doubleArg(0.0)) { player, trackId, context ->
        levels.giveExp(player, trackId, context.getArgument<Double>(EXP))
    }

    // Take exp, lowering level if needed
    changeSubcommand("take", levels, EXP, DoubleArgumentType.doubleArg(0.0)) { player, trackId, context ->
        levels.takeExp(player, trackId, context.getArgument<Double>(EXP))
    }
}

/** Change on a track of the player. Returns `false` if the change isn't supported. */
private typealias LevelChange = (player: Player, trackId: Key, context: CommandContext<CommandSourceStack>) -> Boolean

/** Adds `/mimic levels [name] [track] [amount] (player)` applying the [change]. */
private fun <T : Any> CommandBuilder.changeSubcommand(
    name: String,
    levels: Levels,
    amountName: String,
    amountType: ArgumentType<T>,
    change: LevelChange,
) = literal(name) {
    argument(TRACK, TrackArgumentType) {
        suggestsIds({ sender -> trackHints(levels, sender) }) { trackIds(levels) }
        argument(amountName, amountType) {
            executesPlayer(changeCommandExecutor(levels, change))
            argument(TARGET, ArgumentTypes.player()) {
                executesAny(changeCommandExecutor(levels, change))
            }
        }
    }
}

private fun trackIds(levels: Levels): List<String> = levels.getAllTracks().map { it.key().asString() }

/** Suggests the primary track of the sender and namespaces of tracks. */
private fun trackHints(levels: Levels, sender: CommandSender): List<SuggestionHint> {
    val primaryTrack = (sender as? Player)?.let(levels::getPrimaryTrack)
    val trackIds = trackIds(levels)
    val namespaces = levels.getAllTracks().map { it.key().namespace() }.distinct()

    return listOfNotNull(primaryTrack?.let { SuggestionHint(it.key().asString(), Component.text("Your primary track")) }) +
        namespaces.map { SuggestionHint(collapseFolders("$it:", trackIds), Component.text("Tracks from $it")) }
}

private fun infoCommandExecutor(mimic: Mimic): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayerOrSender(TARGET, sender)
    val levels = mimic.levels
    val primaryTrack = levels.getPrimaryTrack(target)
    val tracks = levels.getTracks(target)

    val message = text {
        appendHeader("Mimic Levels")
        appendStats("Level provider" to (mimic.getPrimaryLevelProviderId() ?: "none"))
        appendKey("${target.name}'s tracks:")
        if (tracks.isEmpty()) appendValue(" none")
        appendLine()
        for (track in tracks) {
            append("  ")
            append(track.name())
            append(" ")
            appendId(track.key().asString())
            if (track == primaryTrack) append(" (primary)", SECONDARY_COLOR)
            append(": ")
            appendValue(levels.getProgress(target, track.key())?.describe() ?: "not loaded")
            appendLine()
        }
        appendCommands(
            suggestedCommand("set", "/mimic levels set [track] [level] (player)", "Set level, resetting exp within the level"),
            suggestedCommand("give", "/mimic levels give [track] [exp] (player)", "Give exp points"),
            suggestedCommand("take", "/mimic levels take [track] [exp] (player)", "Take exp points, lowering level if needed"),
            suggestedCommand("info", "/mimic levels info (player)", "Show player's tracks"),
        )
    }
    sender.sendMessage(message)
}

private fun changeCommandExecutor(levels: Levels, change: LevelChange): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayerOrSender(TARGET, sender)
    val trackId = context.getArgument<Key>(TRACK)
    if (levels.getProgress(target, trackId) == null) {
        throw commandError("${target.name} doesn't have track '${trackId.asString()}', or their data isn't loaded.")
    }
    if (!change(target, trackId, context)) throw commandError("Track '${trackId.asString()}' doesn't support this change.")

    val newProgress = levels.getProgress(target, trackId)
    sender.sendMessage(successText {
        append("New ${target.name}'s progress on ")
        appendId(trackId.asString())
        append(": ${newProgress?.describe() ?: "unknown"}")
    })
}

private fun LevelProgress.describe(): String = buildString {
    append("$level")
    if (hasMaxLevel()) append("/$maxLevel")
    append(" LVL")
    if (hasExp()) {
        append(", %.1f".format(exp))
        if (hasExpToNextLevel()) append("/%.1f".format(expToNextLevel))
        append(" XP")
    }
}

/** Track ID with namespace like `minecraft:level`, shown to the client as a key. */
private object TrackArgumentType : CustomArgumentType<Key, Key> {

    private val invalidTrack = DynamicCommandExceptionType { id ->
        LiteralMessage("Invalid track ID '$id'. Expected ID with namespace, for example minecraft:level.")
    }

    override fun parse(reader: StringReader): Key {
        val start = reader.cursor
        val id = ResourceLocationArgumentType.parse(reader)
        if (':' !in id || !Key.parseable(id)) {
            reader.cursor = start
            throw invalidTrack.createWithContext(reader, id)
        }
        return Key.key(id)
    }

    override fun getNativeType(): ArgumentType<Key> = ArgumentTypes.key()
}

private const val TARGET = "target"
private const val TRACK = "track"
private const val LEVEL = "level"
private const val EXP = "exp"
