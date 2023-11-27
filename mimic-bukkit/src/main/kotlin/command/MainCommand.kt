package ru.endlesscode.mimic.command

import co.aikar.commands.CommandHelp
import co.aikar.commands.MimicCommand
import co.aikar.commands.annotation.*
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import ru.endlesscode.mimic.internal.append
import ru.endlesscode.mimic.internal.appendClickable
import ru.endlesscode.mimic.internal.appendLine
import ru.endlesscode.mimic.internal.buildTextComponent

@CommandAlias("%command")
@CommandPermission("%perm")
internal class MainCommand(
    private val plugin: Plugin,
    private val audiences: BukkitAudiences,
) : MimicCommand() {

    @HelpCommand
    @Description("Show help")
    fun doHelp(help: CommandHelp) {
        help.showHelp()
    }

    @Subcommand("info")
    @Description("Show info about Mimic and loaded services")
    fun info(sender: CommandSender) {
        val message = buildTextComponent {
            appendLine(plugin.description.fullName, NamedTextColor.GREEN)
            color(NamedTextColor.GRAY)
            append("Use ")
            append(createClickableCommand())
            append(" to see or change configs")
        }
        audiences.sender(sender).sendMessage(message)
    }

    private fun createClickableCommand() = buildTextComponent {
        color(NamedTextColor.YELLOW)
        appendClickable(CONFIG_COMMAND, "Click to execute", CONFIG_COMMAND)
    }

    private companion object {
        const val CONFIG_COMMAND = "/mimic config"
    }
}
