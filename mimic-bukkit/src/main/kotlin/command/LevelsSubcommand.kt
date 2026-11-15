// Copyright (c) 2019 Osip Fatkullin, EndlessCode Group and contributors
// SPDX-License-Identifier: MIT

package ru.endlesscode.mimic.command

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.endlesscode.mimic.Mimic
import ru.endlesscode.mimic.impl.mimic.getPrimaryLevelProviderId
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.text
import ru.endlesscode.mimic.level.LevelProgress
import ru.endlesscode.mimic.level.LevelTrack
import ru.endlesscode.mimic.level.Levels
import java.util.*

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
    argument(TRACK, KeyArgumentType("track", "minecraft:level")) {
        suggestsIds({ sender -> trackHints(levels, sender) }) { levels.trackIds() }
        argument(amountName, amountType) {
            executesPlayer(changeCommandExecutor(levels, change))
            argument(TARGET, ArgumentTypes.player()) {
                executesAny(changeCommandExecutor(levels, change))
            }
        }
    }
}

private fun Levels.trackIds(): List<String> = getAllTracks().map { it.key().asString() }

/** Suggests the primary track of the sender and namespaces of tracks. */
private fun trackHints(levels: Levels, sender: CommandSender): List<SuggestionHint> {
    val primaryTrack = (sender as? Player)?.let(levels::getPrimaryTrack)
    return listOfNotNull(primaryTrack?.let { SuggestionHint(it.key().asString(), Component.text("Your primary track")) }) +
            namespaceHints(levels.trackIds()) { "Tracks from $it" }
}

private fun infoCommandExecutor(mimic: Mimic): CommandAction<CommandSender> = { sender, context ->
    val target = context.getPlayerOrSender(TARGET, sender)
    val levels = mimic.levels
    val primaryTrack = levels.getPrimaryTrack(target)
    val tracks = levels.getTracks(target)

    val otherTracks = tracks.filter { it != primaryTrack }

    val message = text {
        appendHeader("Mimic Levels")
        appendProviderStat("Level provider", mimic.getPrimaryLevelProviderId() ?: "none")
        appendKey("${target.name}'s primary track: ")
        if (primaryTrack == null) appendValue("none") else appendTrack(levels, target, primaryTrack)
        appendLine()
        if (otherTracks.isNotEmpty()) {
            appendKey("Other tracks:")
            appendLine()
        }
        for ((namespace, namespaceTracks) in otherTracks.groupBy { it.key().namespace() }) {
            appendValue("  $namespace")
            appendLine()
            for (track in namespaceTracks) {
                append("    ")
                appendTrack(levels, target, track)
                appendLine()
            }
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
        append(": ")
        if (newProgress == null) append("unknown") else appendProgress(newProgress)
    })
}

/** Appends the track name with its ID on hover, and the [player]'s progress on it. */
private fun TextComponent.Builder.appendTrack(levels: Levels, player: Player, track: LevelTrack) {
    appendNamedId(track.name(), track.key().asString())
    append(" ")
    val progress = levels.getProgress(player, track.key())
    if (progress == null) appendValue("not loaded") else appendProgress(progress)
}

/** Appends progress like `Lv 2 / 100 · 50 / 500 XP`, limits are secondary. */
private fun TextComponent.Builder.appendProgress(progress: LevelProgress) {
    appendValue("Lv ${progress.level}")
    if (progress.hasMaxLevel()) append(" / ${progress.maxLevel}", SECONDARY_COLOR)
    if (progress.hasExp()) {
        append(" · ", SECONDARY_COLOR)
        appendValue(formatExp(progress.exp))
        if (progress.hasExpToNextLevel()) append(" / ${formatExp(progress.expToNextLevel)}", SECONDARY_COLOR)
        append(" XP", SECONDARY_COLOR)
    }
}

/** Formats [exp] with one decimal, dropping it if it is zero: `12.5`, `100`. */
private fun formatExp(exp: Double): String = "%.1f".format(Locale.ROOT, exp).removeSuffix(".0")

private const val TARGET = "target"
private const val TRACK = "track"
private const val LEVEL = "level"
private const val EXP = "exp"
